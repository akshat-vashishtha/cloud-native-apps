package com.learning.banking.infrastructure.mapper;

import com.learning.banking.domain.Account;
import com.learning.banking.domain.Card;
import com.learning.banking.domain.Money;
import com.learning.banking.domain.Transaction;
import com.learning.banking.infrastructure.persistence.document.AccountDocument;
import com.learning.banking.infrastructure.persistence.document.CardDocument;
import com.learning.banking.infrastructure.persistence.document.TransactionDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BankingMapper {

    @Mapping(target = "balanceAmount", source = "balance.amount")
    @Mapping(target = "currency", source = "balance.currency")
    @Mapping(target = "version", ignore = true)
    AccountDocument toDocument(Account account);

    @Mapping(target = "balance", expression = "java(mapMoney(document.getBalanceAmount(), document.getCurrency()))")
    Account toDomain(AccountDocument document);

    @Mapping(target = "amount", source = "amount.amount")
    @Mapping(target = "currency", source = "amount.currency")
    TransactionDocument toDocument(Transaction transaction);

    @Mapping(target = "amount", expression = "java(mapMoney(document.getAmount(), document.getCurrency()))")
    Transaction toDomain(TransactionDocument document);

    CardDocument toDocument(Card card);

    Card toDomain(CardDocument document);

    default Money mapMoney(BigDecimal amount, String currency) {
        if (amount == null || currency == null) {
            return null;
        }
        return Money.of(amount, currency);
    }
}
