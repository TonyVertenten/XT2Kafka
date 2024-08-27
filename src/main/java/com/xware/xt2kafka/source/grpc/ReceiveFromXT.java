package com.xware.xt2kafka.source.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import xware.xt.grpc.application.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ReceiveFromXT {
    MsgGrpc.MsgStub xt_async;
    MsgGrpc.MsgBlockingStub xt;

    public ReceiveFromXT(MsgGrpc.MsgStub xt_async, MsgGrpc.MsgBlockingStub xt) {
        this.xt_async = xt_async;
        this.xt = xt;
    }

    public WaitMsgReply ReceiveMsgFromXT(OutputStream outputStream) throws IOException, InterruptedException {
        WaitMsgReply reply = xt.waitMsg(WaitMsgMessage.newBuilder().build());
        for(int i=0; i < reply.getIdsCount(); i++) {
            var msgId = reply.getIds(i).getId();
            int bytesReceived = ReceiveData(outputStream, msgId);
            xt.msgSetStatus(MsgSetStatusMessage.newBuilder().setId(reply.getIds(i).getId()).setCmd(MsgStatusCommand.STATUS_OK).build());

            System.out.println("Received msg " + reply.getIds(i).getId().getMsgid() + " bytes: " + bytesReceived);
        }


        return reply;
    }




    public int ReceiveData(OutputStream dest, MsgIdUri msgid) throws IOException, InterruptedException {

        final CountDownLatch finishLatch = new CountDownLatch(1);

        // Observer for the server replies
        class ResponseObserver implements StreamObserver<ResultByteChunk> {
            ResponseObserver(OutputStream os) {
                ostream = os;
            }

            private OutputStream ostream;

            public Status status = Status.OK;
            private int errorCode = 0;
            public int datasize = 0;

            @Override
            public void onNext(ResultByteChunk r) {
                if (r.getErrorcode() == 0) {
                    try {
                        ostream.write(r.getChunk().toByteArray());
                        datasize += r.getChunk().size();
                    }
                    catch (IOException ex) {
                        status = Status.INTERNAL.withCause(ex);
                        finishLatch.countDown();
                    }
                }
                else {
                    setErrorCode(r.getErrorcode());
                    finishLatch.countDown();
                }
            }
            @Override
            public void onError(Throwable t) {
                status = Status.fromThrowable(t);
                finishLatch.countDown();
            }
            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
            public void setErrorCode(int errorCode) {
                this.errorCode = errorCode;
            }
        };

        ResponseObserver responseObserver = new ResponseObserver(dest);
        GetMsgDataMessage request = GetMsgDataMessage.newBuilder().setId(msgid).build();
        xt_async.getMsgData(request, responseObserver);

        finishLatch.await(1, TimeUnit.MINUTES);

        if (!responseObserver.status.isOk())
            throw responseObserver.status.asRuntimeException();

        return responseObserver.datasize;
    }

}
