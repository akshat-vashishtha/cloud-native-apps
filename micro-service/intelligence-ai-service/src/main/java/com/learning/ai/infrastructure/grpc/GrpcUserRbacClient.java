package com.learning.ai.infrastructure.grpc;

import com.learning.ai.infrastructure.config.AiServiceProperties;
import com.learning.user.grpc.v1.GetUserProfileRequest;
import com.learning.user.grpc.v1.GetUserProfileResponse;
import com.learning.user.grpc.v1.UserRbacGrpcServiceGrpc;
import com.learning.user.grpc.v1.ValidatePermissionsRequest;
import com.learning.user.grpc.v1.ValidatePermissionsResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcUserRbacClient {

    private static final Logger log = LoggerFactory.getLogger(GrpcUserRbacClient.class);

    @GrpcClient("user-rbac-service")
    private UserRbacGrpcServiceGrpc.UserRbacGrpcServiceBlockingStub userRbacStub;

    private final AiServiceProperties properties;

    public GrpcUserRbacClient(AiServiceProperties properties) {
        this.properties = properties;
    }

    public UserProfileSummary getUserProfile(String userId) {
        log.info("gRPC Client -> UserRbacGrpcService.getUserProfile(userId={})", userId);
        GetUserProfileResponse response = userRbacStub
                .withDeadlineAfter(properties.grpcDeadlineMs(), TimeUnit.MILLISECONDS)
                .getUserProfile(GetUserProfileRequest.newBuilder().setUserId(userId).build());

        return new UserProfileSummary(
                response.getUserId(),
                response.getEmail(),
                response.getFullName(),
                List.copyOf(response.getRolesList()),
                List.copyOf(response.getPermissionsList()),
                response.getStatus()
        );
    }

    public boolean hasPermission(String userId, String requiredPermission) {
        ValidatePermissionsResponse response = userRbacStub
                .withDeadlineAfter(properties.grpcDeadlineMs(), TimeUnit.MILLISECONDS)
                .validatePermissions(ValidatePermissionsRequest.newBuilder()
                        .setUserId(userId)
                        .setRequiredPermission(requiredPermission)
                        .build());
        return response.getIsAuthorized();
    }

    public record UserProfileSummary(
            String userId,
            String email,
            String fullName,
            List<String> roles,
            List<String> permissions,
            String status
    ) {}
}
