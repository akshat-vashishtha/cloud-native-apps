package com.learning.banking.presentation.grpc;

import com.learning.banking.application.dto.AccountResponse;
import com.learning.banking.application.dto.TransferFundsRequest;
import com.learning.banking.application.dto.TransferResponse;
import com.learning.banking.application.service.BankingService;
import com.learning.banking.domain.enums.CardStatus;
import com.learning.banking.grpc.v1.BankingGrpcServiceGrpc;
import com.learning.banking.grpc.v1.GetBalanceRequest;
import com.learning.banking.grpc.v1.GetBalanceResponse;
import com.learning.banking.grpc.v1.TransferFundsResponse;
import com.learning.banking.grpc.v1.UpdateCardRequest;
import com.learning.banking.grpc.v1.UpdateCardResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@GrpcService
public class BankingGrpcServiceImpl extends BankingGrpcServiceGrpc.BankingGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BankingGrpcServiceImpl.class);

    private final BankingService bankingService;

    public BankingGrpcServiceImpl(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @Override
    public void getAccountBalance(GetBalanceRequest request, StreamObserver<GetBalanceResponse> responseObserver) {
        try {
            AccountResponse account = bankingService.getAccountById(request.getAccountId());

            // Validate that the requesting user owns the account
            if (!account.userId().equals(request.getUserId())) {
                responseObserver.onError(
                        Status.PERMISSION_DENIED
                                .withDescription("User is not authorized to view this account")
                                .asRuntimeException()
                );
                return;
            }

            GetBalanceResponse response = GetBalanceResponse.newBuilder()
                    .setAccountId(account.id())
                    .setBalance(account.balance().doubleValue())
                    .setCurrency(account.currency())
                    .setStatus(account.status())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error retrieving balance for account: {}", request.getAccountId(), e);
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void transferFunds(com.learning.banking.grpc.v1.TransferFundsRequest request,
                              StreamObserver<TransferFundsResponse> responseObserver) {
        try {
            TransferFundsRequest dto = new TransferFundsRequest(
                    request.getSourceAccountId(),
                    request.getTargetAccountId(),
                    BigDecimal.valueOf(request.getAmount()),
                    request.getCurrency(),
                    request.getDescription().isEmpty() ? "AI Assistant Transfer" : request.getDescription()
            );

            // Pass request.getUserId() so caller ownership is verified!
            TransferResponse transferResponse = bankingService.transferFunds(dto, request.getUserId(), null);

            TransferFundsResponse response = TransferFundsResponse.newBuilder()
                    .setTransactionId(transferResponse.transactionId())
                    .setStatus(transferResponse.status())
                    .setMessage("Transfer completed successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC fund transfer failed from account: {}", request.getSourceAccountId(), e);
            responseObserver.onError(
                    Status.FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void updateCardStatus(UpdateCardRequest request, StreamObserver<UpdateCardResponse> responseObserver) {
        try {
            CardStatus cardStatus = CardStatus.valueOf(request.getStatus().toUpperCase());
            bankingService.updateCardStatus(request.getCardId(), request.getUserId(), cardStatus);

            UpdateCardResponse response = UpdateCardResponse.newBuilder()
                    .setCardId(request.getCardId())
                    .setStatus(cardStatus.name())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("gRPC card status update failed for card: {}", request.getCardId(), e);
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }
}
