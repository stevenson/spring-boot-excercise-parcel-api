package com.stevenson.parcel.service;

import com.stevenson.parcel.api.controller.exception.ApiRejectException;
import com.stevenson.parcel.model.Parcel;
import com.stevenson.parcel.model.Rule;
import com.stevenson.parcel.model.Voucher;
import com.stevenson.parcel.repo.ParcelRepo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Service
@Slf4j
public class DefaultParcelService implements ParcelService{
    private final ParcelRepo repo;
    private final RuleService ruleService;
    private final VoucherService voucherService;

    @Autowired
    public DefaultParcelService(ParcelRepo repo, RuleService ruleService, VoucherService voucherService) {
        this.repo = repo;
        this.ruleService = ruleService;
        this.voucherService = voucherService;
    }

    @Override
    public Parcel create(Parcel parcel) {
        applyRules(parcel);
        applyVoucher(parcel);
        if(parcel.getCost() == 0){
            throw new ApiRejectException("Parcel cost not computed Rejection due to rule.");
        }
        return repo.save(parcel);
    }

    @Override
    public List<Parcel> retrieveAll() {
        return stream(repo.findAll().spliterator(), false).collect(toList());
    }

    public List<Parcel> retrieveAll(Pageable paging) {
        return stream(repo.findAll(paging)
                .spliterator(), false)
                .collect(toList());
    }

    @Override
    public Optional<Parcel> retrieve(long id) {
        return repo.findById(id);
    }

    private Parcel applyRules(Parcel parcel){
        Pageable paging = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "priority"));
        List<Rule> rules = ruleService.retrieveAll(paging);
        for(Rule rule: rules) {
            if (rule.applies(parcel)) {
                parcel.applyRule(rule);
                break;
            }
        }
        return parcel;
    }

    private Parcel applyVoucher(Parcel parcel){
        if(parcel.getVoucherCode()!= null ){
            Optional<Voucher> optional = voucherService.retrieve(parcel.getVoucherCode());
            if(!optional.isEmpty() && optional.get().applies()){
                parcel.applyVoucher(optional.get());
            }
        }
        return parcel;
    }
}
