package com.stevenson.parcel.api.controller;

import com.stevenson.parcel.api.dto.ParcelRequest;
import com.stevenson.parcel.api.dto.ParcelResponse;
import com.stevenson.parcel.model.Parcel;
import com.stevenson.parcel.model.Voucher;
import com.stevenson.parcel.service.DefaultParcelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DefaultParcelControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private DefaultParcelService mockParcelService;

    @Test
    void addParcelShouldComputeDefaultForNormalParcel() {
        // given
        double weight = 10, length = 3, width = 3, height = 3;
        ParcelRequest request = new ParcelRequest( weight, length, width, height, null);
        Parcel normalParcel = Parcel.builder()
                .weight(weight)
                .length(length)
                .width(width)
                .height(height)
                .build();
        given(this.mockParcelService.create(any())).willReturn(normalParcel);

        Double expectedVolume = request.getHeight() * request.getWidth() * request.getLength();
        Double expectedCost = expectedVolume*0.4;
        // scenario
        ResponseEntity<ParcelResponse> response = restTemplate.postForEntity(
                "http://localhost:"+ port + "/api/v1/parcels",
                request,
                ParcelResponse.class);
        assertThat(response, is(notNullValue()));
        assertThat(response.getBody().getVolume(), is(expectedVolume));
        assertThat(response.getBody().getCost(), is(expectedCost));
        assertThat(response.getBody().getStatus(), is("accepted"));
        assertThat(response.getStatusCodeValue(), is(201));
    }

    @Test
    void addParcelShouldApplyVouchers() {
        // given
        double weight = 10, length = 10, width = 3, height = 3;
        ParcelRequest request = new ParcelRequest( weight, length, width, height, "MYNT");
        Parcel parcelWithVoucher = Parcel.builder()
                .weight(weight)
                .length(length)
                .width(width)
                .height(height)
                .build();
        Voucher voucher = Voucher.builder()
                .code("MYNT")
                .discount(10)
                .expiry(LocalDate.now().plusDays(2))
                .build();
        Optional<Voucher> vouchers = Optional.ofNullable(voucher);
        Double expectedVolume = request.getHeight() * request.getWidth() * request.getLength();
        Double expectedCost = expectedVolume*0.4 - voucher.getDiscount();
        parcelWithVoucher.setVolume(expectedVolume);
        parcelWithVoucher.setCost(expectedCost);

        given(this.mockParcelService.create(any())).willReturn(parcelWithVoucher);

        // scenario
        ResponseEntity<ParcelResponse> response = restTemplate.postForEntity(
                "http://localhost:"+ port + "/api/v1/parcels",
                request,
                ParcelResponse.class);
        assertThat(response, is(notNullValue()));
        assertThat(response.getBody().getVolume(), is(expectedVolume));
        assertThat(response.getBody().getCost(), is(expectedCost));
        assertThat(response.getBody().getStatus(), is("accepted"));
        assertThat(response.getStatusCodeValue(), is(201));
    }
//    @Test
//    void addParcelShouldApplyRules() {
//        // given
//        double weight = 100, length = 3, width = 3, height = 3;
//        ParcelRequest request = new ParcelRequest( weight, length, width, height, null);
//        Parcel normalParcel = Parcel.builder()
//                .weight(weight)
//                .length(length)
//                .width(width)
//                .height(height)
//                .build();
//        Rule rule1 = Rule.builder()
//                .priority(1)
//                .name("reject")
//                .param("weight")
//                .condition("exceeds")
//                .threshold(50)
//                .rate(0.0)
//                .factor("weight")
//                .build();
//        List<Rule> rules = new ArrayList<>();
//        rules.add(rule1);
//        given(this.mockParcelService.create(any())).willReturn(normalParcel);
//        given(this.mockRuleService.retrieveAll(any())).willReturn(rules);
//
//        Double expectedVolume = request.getHeight() * request.getWidth() * request.getLength();
//        Double expectedCost = 0.0;
//        // scenario
//        ResponseEntity<ParcelResponse> response = restTemplate.postForEntity(
//                "http://localhost:"+ port + "/api/v1/parcels",
//                request,
//                ParcelResponse.class);
//        assertThat(response, is(notNullValue()));
//        assertThat(response.getBody().getVolume(), is(expectedVolume));
//        assertThat(response.getBody().getCost(), is(expectedCost));
//        assertThat(response.getBody().getStatus(), is("rejected"));
//        assertThat(response.getStatusCodeValue(), is(400));
//    }
}