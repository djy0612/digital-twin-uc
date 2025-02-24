package org.example.policy.service;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.31.0)",
    comments = "Source: policy.proto")
public final class PolicyServiceGrpc {

  private PolicyServiceGrpc() {}

  public static final String SERVICE_NAME = "org.example.policy.service.PolicyService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.example.policy.service.PolicyRequest,
      org.example.policy.service.PolicyResponse> getEvaluatePolicyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "evaluatePolicy",
      requestType = org.example.policy.service.PolicyRequest.class,
      responseType = org.example.policy.service.PolicyResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.policy.service.PolicyRequest,
      org.example.policy.service.PolicyResponse> getEvaluatePolicyMethod() {
    io.grpc.MethodDescriptor<org.example.policy.service.PolicyRequest, org.example.policy.service.PolicyResponse> getEvaluatePolicyMethod;
    if ((getEvaluatePolicyMethod = PolicyServiceGrpc.getEvaluatePolicyMethod) == null) {
      synchronized (PolicyServiceGrpc.class) {
        if ((getEvaluatePolicyMethod = PolicyServiceGrpc.getEvaluatePolicyMethod) == null) {
          PolicyServiceGrpc.getEvaluatePolicyMethod = getEvaluatePolicyMethod =
              io.grpc.MethodDescriptor.<org.example.policy.service.PolicyRequest, org.example.policy.service.PolicyResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "evaluatePolicy"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.policy.service.PolicyRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.policy.service.PolicyResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PolicyServiceMethodDescriptorSupplier("evaluatePolicy"))
              .build();
        }
      }
    }
    return getEvaluatePolicyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.policy.service.PolicyRequest,
      org.example.policy.service.PolicyResponse> getValidatePolicyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "validatePolicy",
      requestType = org.example.policy.service.PolicyRequest.class,
      responseType = org.example.policy.service.PolicyResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.policy.service.PolicyRequest,
      org.example.policy.service.PolicyResponse> getValidatePolicyMethod() {
    io.grpc.MethodDescriptor<org.example.policy.service.PolicyRequest, org.example.policy.service.PolicyResponse> getValidatePolicyMethod;
    if ((getValidatePolicyMethod = PolicyServiceGrpc.getValidatePolicyMethod) == null) {
      synchronized (PolicyServiceGrpc.class) {
        if ((getValidatePolicyMethod = PolicyServiceGrpc.getValidatePolicyMethod) == null) {
          PolicyServiceGrpc.getValidatePolicyMethod = getValidatePolicyMethod =
              io.grpc.MethodDescriptor.<org.example.policy.service.PolicyRequest, org.example.policy.service.PolicyResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "validatePolicy"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.policy.service.PolicyRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.policy.service.PolicyResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PolicyServiceMethodDescriptorSupplier("validatePolicy"))
              .build();
        }
      }
    }
    return getValidatePolicyMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.policy.service.ValidateRequest,
      org.example.policy.service.ValidateResponse> getValidateRequestMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "validateRequest",
      requestType = org.example.policy.service.ValidateRequest.class,
      responseType = org.example.policy.service.ValidateResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.policy.service.ValidateRequest,
      org.example.policy.service.ValidateResponse> getValidateRequestMethod() {
    io.grpc.MethodDescriptor<org.example.policy.service.ValidateRequest, org.example.policy.service.ValidateResponse> getValidateRequestMethod;
    if ((getValidateRequestMethod = PolicyServiceGrpc.getValidateRequestMethod) == null) {
      synchronized (PolicyServiceGrpc.class) {
        if ((getValidateRequestMethod = PolicyServiceGrpc.getValidateRequestMethod) == null) {
          PolicyServiceGrpc.getValidateRequestMethod = getValidateRequestMethod =
              io.grpc.MethodDescriptor.<org.example.policy.service.ValidateRequest, org.example.policy.service.ValidateResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "validateRequest"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.policy.service.ValidateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.policy.service.ValidateResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PolicyServiceMethodDescriptorSupplier("validateRequest"))
              .build();
        }
      }
    }
    return getValidateRequestMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.example.policy.service.UploadPolicyRequest,
      org.example.policy.service.UploadPolicyResponse> getUploadPolicyMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "uploadPolicy",
      requestType = org.example.policy.service.UploadPolicyRequest.class,
      responseType = org.example.policy.service.UploadPolicyResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<org.example.policy.service.UploadPolicyRequest,
      org.example.policy.service.UploadPolicyResponse> getUploadPolicyMethod() {
    io.grpc.MethodDescriptor<org.example.policy.service.UploadPolicyRequest, org.example.policy.service.UploadPolicyResponse> getUploadPolicyMethod;
    if ((getUploadPolicyMethod = PolicyServiceGrpc.getUploadPolicyMethod) == null) {
      synchronized (PolicyServiceGrpc.class) {
        if ((getUploadPolicyMethod = PolicyServiceGrpc.getUploadPolicyMethod) == null) {
          PolicyServiceGrpc.getUploadPolicyMethod = getUploadPolicyMethod =
              io.grpc.MethodDescriptor.<org.example.policy.service.UploadPolicyRequest, org.example.policy.service.UploadPolicyResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "uploadPolicy"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.policy.service.UploadPolicyRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.example.policy.service.UploadPolicyResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PolicyServiceMethodDescriptorSupplier("uploadPolicy"))
              .build();
        }
      }
    }
    return getUploadPolicyMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PolicyServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PolicyServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PolicyServiceStub>() {
        @java.lang.Override
        public PolicyServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PolicyServiceStub(channel, callOptions);
        }
      };
    return PolicyServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PolicyServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PolicyServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PolicyServiceBlockingStub>() {
        @java.lang.Override
        public PolicyServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PolicyServiceBlockingStub(channel, callOptions);
        }
      };
    return PolicyServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PolicyServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PolicyServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PolicyServiceFutureStub>() {
        @java.lang.Override
        public PolicyServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PolicyServiceFutureStub(channel, callOptions);
        }
      };
    return PolicyServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class PolicyServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void evaluatePolicy(org.example.policy.service.PolicyRequest request,
        io.grpc.stub.StreamObserver<org.example.policy.service.PolicyResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getEvaluatePolicyMethod(), responseObserver);
    }

    /**
     */
    public void validatePolicy(org.example.policy.service.PolicyRequest request,
        io.grpc.stub.StreamObserver<org.example.policy.service.PolicyResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getValidatePolicyMethod(), responseObserver);
    }

    /**
     */
    public void validateRequest(org.example.policy.service.ValidateRequest request,
        io.grpc.stub.StreamObserver<org.example.policy.service.ValidateResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getValidateRequestMethod(), responseObserver);
    }

    /**
     */
    public void uploadPolicy(org.example.policy.service.UploadPolicyRequest request,
        io.grpc.stub.StreamObserver<org.example.policy.service.UploadPolicyResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getUploadPolicyMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getEvaluatePolicyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.example.policy.service.PolicyRequest,
                org.example.policy.service.PolicyResponse>(
                  this, METHODID_EVALUATE_POLICY)))
          .addMethod(
            getValidatePolicyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.example.policy.service.PolicyRequest,
                org.example.policy.service.PolicyResponse>(
                  this, METHODID_VALIDATE_POLICY)))
          .addMethod(
            getValidateRequestMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.example.policy.service.ValidateRequest,
                org.example.policy.service.ValidateResponse>(
                  this, METHODID_VALIDATE_REQUEST)))
          .addMethod(
            getUploadPolicyMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                org.example.policy.service.UploadPolicyRequest,
                org.example.policy.service.UploadPolicyResponse>(
                  this, METHODID_UPLOAD_POLICY)))
          .build();
    }
  }

  /**
   */
  public static final class PolicyServiceStub extends io.grpc.stub.AbstractAsyncStub<PolicyServiceStub> {
    private PolicyServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PolicyServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PolicyServiceStub(channel, callOptions);
    }

    /**
     */
    public void evaluatePolicy(org.example.policy.service.PolicyRequest request,
        io.grpc.stub.StreamObserver<org.example.policy.service.PolicyResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getEvaluatePolicyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void validatePolicy(org.example.policy.service.PolicyRequest request,
        io.grpc.stub.StreamObserver<org.example.policy.service.PolicyResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getValidatePolicyMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void validateRequest(org.example.policy.service.ValidateRequest request,
        io.grpc.stub.StreamObserver<org.example.policy.service.ValidateResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getValidateRequestMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void uploadPolicy(org.example.policy.service.UploadPolicyRequest request,
        io.grpc.stub.StreamObserver<org.example.policy.service.UploadPolicyResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getUploadPolicyMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class PolicyServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<PolicyServiceBlockingStub> {
    private PolicyServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PolicyServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PolicyServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public org.example.policy.service.PolicyResponse evaluatePolicy(org.example.policy.service.PolicyRequest request) {
      return blockingUnaryCall(
          getChannel(), getEvaluatePolicyMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.example.policy.service.PolicyResponse validatePolicy(org.example.policy.service.PolicyRequest request) {
      return blockingUnaryCall(
          getChannel(), getValidatePolicyMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.example.policy.service.ValidateResponse validateRequest(org.example.policy.service.ValidateRequest request) {
      return blockingUnaryCall(
          getChannel(), getValidateRequestMethod(), getCallOptions(), request);
    }

    /**
     */
    public org.example.policy.service.UploadPolicyResponse uploadPolicy(org.example.policy.service.UploadPolicyRequest request) {
      return blockingUnaryCall(
          getChannel(), getUploadPolicyMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class PolicyServiceFutureStub extends io.grpc.stub.AbstractFutureStub<PolicyServiceFutureStub> {
    private PolicyServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PolicyServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PolicyServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.policy.service.PolicyResponse> evaluatePolicy(
        org.example.policy.service.PolicyRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getEvaluatePolicyMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.policy.service.PolicyResponse> validatePolicy(
        org.example.policy.service.PolicyRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getValidatePolicyMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.policy.service.ValidateResponse> validateRequest(
        org.example.policy.service.ValidateRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getValidateRequestMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.example.policy.service.UploadPolicyResponse> uploadPolicy(
        org.example.policy.service.UploadPolicyRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getUploadPolicyMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_EVALUATE_POLICY = 0;
  private static final int METHODID_VALIDATE_POLICY = 1;
  private static final int METHODID_VALIDATE_REQUEST = 2;
  private static final int METHODID_UPLOAD_POLICY = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final PolicyServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(PolicyServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_EVALUATE_POLICY:
          serviceImpl.evaluatePolicy((org.example.policy.service.PolicyRequest) request,
              (io.grpc.stub.StreamObserver<org.example.policy.service.PolicyResponse>) responseObserver);
          break;
        case METHODID_VALIDATE_POLICY:
          serviceImpl.validatePolicy((org.example.policy.service.PolicyRequest) request,
              (io.grpc.stub.StreamObserver<org.example.policy.service.PolicyResponse>) responseObserver);
          break;
        case METHODID_VALIDATE_REQUEST:
          serviceImpl.validateRequest((org.example.policy.service.ValidateRequest) request,
              (io.grpc.stub.StreamObserver<org.example.policy.service.ValidateResponse>) responseObserver);
          break;
        case METHODID_UPLOAD_POLICY:
          serviceImpl.uploadPolicy((org.example.policy.service.UploadPolicyRequest) request,
              (io.grpc.stub.StreamObserver<org.example.policy.service.UploadPolicyResponse>) responseObserver);
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

  private static abstract class PolicyServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PolicyServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.example.policy.service.Policy.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PolicyService");
    }
  }

  private static final class PolicyServiceFileDescriptorSupplier
      extends PolicyServiceBaseDescriptorSupplier {
    PolicyServiceFileDescriptorSupplier() {}
  }

  private static final class PolicyServiceMethodDescriptorSupplier
      extends PolicyServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    PolicyServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (PolicyServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PolicyServiceFileDescriptorSupplier())
              .addMethod(getEvaluatePolicyMethod())
              .addMethod(getValidatePolicyMethod())
              .addMethod(getValidateRequestMethod())
              .addMethod(getUploadPolicyMethod())
              .build();
        }
      }
    }
    return result;
  }
}
