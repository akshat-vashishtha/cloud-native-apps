package com.learning.user.grpc.v1;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.63.0)",
    comments = "Source: user_rbac_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class UserRbacGrpcServiceGrpc {

  private UserRbacGrpcServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "user.v1.UserRbacGrpcService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.learning.user.grpc.v1.GetUserProfileRequest,
      com.learning.user.grpc.v1.GetUserProfileResponse> getGetUserProfileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUserProfile",
      requestType = com.learning.user.grpc.v1.GetUserProfileRequest.class,
      responseType = com.learning.user.grpc.v1.GetUserProfileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.learning.user.grpc.v1.GetUserProfileRequest,
      com.learning.user.grpc.v1.GetUserProfileResponse> getGetUserProfileMethod() {
    io.grpc.MethodDescriptor<com.learning.user.grpc.v1.GetUserProfileRequest, com.learning.user.grpc.v1.GetUserProfileResponse> getGetUserProfileMethod;
    if ((getGetUserProfileMethod = UserRbacGrpcServiceGrpc.getGetUserProfileMethod) == null) {
      synchronized (UserRbacGrpcServiceGrpc.class) {
        if ((getGetUserProfileMethod = UserRbacGrpcServiceGrpc.getGetUserProfileMethod) == null) {
          UserRbacGrpcServiceGrpc.getGetUserProfileMethod = getGetUserProfileMethod =
              io.grpc.MethodDescriptor.<com.learning.user.grpc.v1.GetUserProfileRequest, com.learning.user.grpc.v1.GetUserProfileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUserProfile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.learning.user.grpc.v1.GetUserProfileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.learning.user.grpc.v1.GetUserProfileResponse.getDefaultInstance()))
              .setSchemaDescriptor(new UserRbacGrpcServiceMethodDescriptorSupplier("GetUserProfile"))
              .build();
        }
      }
    }
    return getGetUserProfileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.learning.user.grpc.v1.ValidatePermissionsRequest,
      com.learning.user.grpc.v1.ValidatePermissionsResponse> getValidatePermissionsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ValidatePermissions",
      requestType = com.learning.user.grpc.v1.ValidatePermissionsRequest.class,
      responseType = com.learning.user.grpc.v1.ValidatePermissionsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.learning.user.grpc.v1.ValidatePermissionsRequest,
      com.learning.user.grpc.v1.ValidatePermissionsResponse> getValidatePermissionsMethod() {
    io.grpc.MethodDescriptor<com.learning.user.grpc.v1.ValidatePermissionsRequest, com.learning.user.grpc.v1.ValidatePermissionsResponse> getValidatePermissionsMethod;
    if ((getValidatePermissionsMethod = UserRbacGrpcServiceGrpc.getValidatePermissionsMethod) == null) {
      synchronized (UserRbacGrpcServiceGrpc.class) {
        if ((getValidatePermissionsMethod = UserRbacGrpcServiceGrpc.getValidatePermissionsMethod) == null) {
          UserRbacGrpcServiceGrpc.getValidatePermissionsMethod = getValidatePermissionsMethod =
              io.grpc.MethodDescriptor.<com.learning.user.grpc.v1.ValidatePermissionsRequest, com.learning.user.grpc.v1.ValidatePermissionsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ValidatePermissions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.learning.user.grpc.v1.ValidatePermissionsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.learning.user.grpc.v1.ValidatePermissionsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new UserRbacGrpcServiceMethodDescriptorSupplier("ValidatePermissions"))
              .build();
        }
      }
    }
    return getValidatePermissionsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UserRbacGrpcServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserRbacGrpcServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserRbacGrpcServiceStub>() {
        @java.lang.Override
        public UserRbacGrpcServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserRbacGrpcServiceStub(channel, callOptions);
        }
      };
    return UserRbacGrpcServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UserRbacGrpcServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserRbacGrpcServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserRbacGrpcServiceBlockingStub>() {
        @java.lang.Override
        public UserRbacGrpcServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserRbacGrpcServiceBlockingStub(channel, callOptions);
        }
      };
    return UserRbacGrpcServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UserRbacGrpcServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserRbacGrpcServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserRbacGrpcServiceFutureStub>() {
        @java.lang.Override
        public UserRbacGrpcServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserRbacGrpcServiceFutureStub(channel, callOptions);
        }
      };
    return UserRbacGrpcServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void getUserProfile(com.learning.user.grpc.v1.GetUserProfileRequest request,
        io.grpc.stub.StreamObserver<com.learning.user.grpc.v1.GetUserProfileResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetUserProfileMethod(), responseObserver);
    }

    /**
     */
    default void validatePermissions(com.learning.user.grpc.v1.ValidatePermissionsRequest request,
        io.grpc.stub.StreamObserver<com.learning.user.grpc.v1.ValidatePermissionsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getValidatePermissionsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service UserRbacGrpcService.
   */
  public static abstract class UserRbacGrpcServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return UserRbacGrpcServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service UserRbacGrpcService.
   */
  public static final class UserRbacGrpcServiceStub
      extends io.grpc.stub.AbstractAsyncStub<UserRbacGrpcServiceStub> {
    private UserRbacGrpcServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserRbacGrpcServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserRbacGrpcServiceStub(channel, callOptions);
    }

    /**
     */
    public void getUserProfile(com.learning.user.grpc.v1.GetUserProfileRequest request,
        io.grpc.stub.StreamObserver<com.learning.user.grpc.v1.GetUserProfileResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetUserProfileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void validatePermissions(com.learning.user.grpc.v1.ValidatePermissionsRequest request,
        io.grpc.stub.StreamObserver<com.learning.user.grpc.v1.ValidatePermissionsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getValidatePermissionsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service UserRbacGrpcService.
   */
  public static final class UserRbacGrpcServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<UserRbacGrpcServiceBlockingStub> {
    private UserRbacGrpcServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserRbacGrpcServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserRbacGrpcServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.learning.user.grpc.v1.GetUserProfileResponse getUserProfile(com.learning.user.grpc.v1.GetUserProfileRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetUserProfileMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.learning.user.grpc.v1.ValidatePermissionsResponse validatePermissions(com.learning.user.grpc.v1.ValidatePermissionsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getValidatePermissionsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service UserRbacGrpcService.
   */
  public static final class UserRbacGrpcServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<UserRbacGrpcServiceFutureStub> {
    private UserRbacGrpcServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserRbacGrpcServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserRbacGrpcServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.learning.user.grpc.v1.GetUserProfileResponse> getUserProfile(
        com.learning.user.grpc.v1.GetUserProfileRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetUserProfileMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.learning.user.grpc.v1.ValidatePermissionsResponse> validatePermissions(
        com.learning.user.grpc.v1.ValidatePermissionsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getValidatePermissionsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_USER_PROFILE = 0;
  private static final int METHODID_VALIDATE_PERMISSIONS = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_USER_PROFILE:
          serviceImpl.getUserProfile((com.learning.user.grpc.v1.GetUserProfileRequest) request,
              (io.grpc.stub.StreamObserver<com.learning.user.grpc.v1.GetUserProfileResponse>) responseObserver);
          break;
        case METHODID_VALIDATE_PERMISSIONS:
          serviceImpl.validatePermissions((com.learning.user.grpc.v1.ValidatePermissionsRequest) request,
              (io.grpc.stub.StreamObserver<com.learning.user.grpc.v1.ValidatePermissionsResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetUserProfileMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.learning.user.grpc.v1.GetUserProfileRequest,
              com.learning.user.grpc.v1.GetUserProfileResponse>(
                service, METHODID_GET_USER_PROFILE)))
        .addMethod(
          getValidatePermissionsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.learning.user.grpc.v1.ValidatePermissionsRequest,
              com.learning.user.grpc.v1.ValidatePermissionsResponse>(
                service, METHODID_VALIDATE_PERMISSIONS)))
        .build();
  }

  private static abstract class UserRbacGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UserRbacGrpcServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.learning.user.grpc.v1.UserRbacService.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UserRbacGrpcService");
    }
  }

  private static final class UserRbacGrpcServiceFileDescriptorSupplier
      extends UserRbacGrpcServiceBaseDescriptorSupplier {
    UserRbacGrpcServiceFileDescriptorSupplier() {}
  }

  private static final class UserRbacGrpcServiceMethodDescriptorSupplier
      extends UserRbacGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    UserRbacGrpcServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (UserRbacGrpcServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UserRbacGrpcServiceFileDescriptorSupplier())
              .addMethod(getGetUserProfileMethod())
              .addMethod(getValidatePermissionsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
