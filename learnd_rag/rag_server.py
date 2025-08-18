import grpc
from concurrent import futures
import event_dispatcher_pb2
import event_dispatcher_pb2_grpc
from rag import *

class EventDispatcherServicer(event_dispatcher_pb2_grpc.EventDispatcherServicer):
    def DispatchCardUpdates(self, request, context):
        print("Received request:")
        print(request)
        result = compile_graph(request)

        responseObj = event_dispatcher_pb2.DispatchResultGRPC(
            success = True,
            question=result.question,
            answer=result.answer
        )
        return responseObj

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    event_dispatcher_pb2_grpc.add_EventDispatcherServicer_to_server(EventDispatcherServicer(), server)
    server.add_insecure_port('[::]:50051')  # Match port used in Java client
    server.start()
    print("gRPC server started on port 50051")
    server.wait_for_termination()

if __name__ == '__main__':
    serve()
