package no.entur.grpc.interceptor.server;

/*-
 * #%L
 * Protobuf validator
 * %%
 * Copyright (C) 2017 - 2018 Original authors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.protobuf.GeneratedMessageV3;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import no.entur.protobuf.validation.ProtobufValidator;

/**
 * @author Serious
 * @author seime
 * @date 2017/6/26
 */
public class ValidationInterceptor implements ServerInterceptor {

	private ProtobufValidator validator = new ProtobufValidator();

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> call, final Metadata headers,
			final ServerCallHandler<ReqT, RespT> next) {
		ServerCall.Listener<ReqT> listener = next.startCall(call, headers);
		return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
			@Override
			public void onMessage(ReqT message) {
				GeneratedMessageV3 messageV3 = (GeneratedMessageV3) message;
				try {
					validator.validate(messageV3);
					super.onMessage(message);
				} catch (Exception e) {
					Status status = Status.INVALID_ARGUMENT.withDescription(e.getMessage());
					// call.close(status, headers);
					throw new StatusRuntimeException(status);
				}
			}
		};
	}
}