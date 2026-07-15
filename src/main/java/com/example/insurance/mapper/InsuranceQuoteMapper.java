package com.example.insurance.mapper;

import com.example.insurance.dto.QuoteResponse;
import com.example.insurance.entity.InsuranceQuote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InsuranceQuoteMapper extends BaseMapper<InsuranceQuote, QuoteResponse> {

    InsuranceQuoteMapper INSTANCE = Mappers.getMapper(InsuranceQuoteMapper.class);

    @Override
    @Mapping(source = "workflowId", target = "workflowId")
    @Mapping(source = "customerName", target = "customerName")
    @Mapping(source = "age", target = "age")
    @Mapping(source = "vehicleType", target = "vehicleType")
    @Mapping(source = "vehicleValue", target = "vehicleValue")
    @Mapping(source = "claimHistory", target = "claimHistory")
    @Mapping(source = "premium", target = "premiumAmount")
    @Mapping(source = "discount", target = "discount")
    @Mapping(source = "riskCategory", target = "riskCategory")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdDate")
    QuoteResponse toDto(InsuranceQuote entity);

    @Override
    @Mapping(source = "workflowId", target = "workflowId")
    @Mapping(source = "customerName", target = "customerName")
    @Mapping(source = "age", target = "age")
    @Mapping(source = "vehicleType", target = "vehicleType")
    @Mapping(source = "vehicleValue", target = "vehicleValue")
    @Mapping(source = "claimHistory", target = "claimHistory")
    @Mapping(source = "premiumAmount", target = "premium")
    @Mapping(source = "discount", target = "discount")
    @Mapping(source = "riskCategory", target = "riskCategory")
    @Mapping(source = "status", target = "status")
    InsuranceQuote toEntity(QuoteResponse dto);

    void updateEntityFromDto(QuoteResponse dto, @MappingTarget InsuranceQuote entity);
}
