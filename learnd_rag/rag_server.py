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

        print("Generated result from RAG pipeline: ")
        print(result)
        if 'grade_hallucination' in result:
            return event_dispatcher_pb2.DispatchResultGRPC(
                success = False,
                question=result['grade_hallucination']['generated_question'],
                answer=[result['grade_hallucination']['generated_answer']]
            )
        else:
            return event_dispatcher_pb2.DispatchResultGRPC(
            success = True,
            question=result['generate_recommendation']['generated_question'],
            answer=[result['generate_recommendation']['generated_answer']]
        )


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    event_dispatcher_pb2_grpc.add_EventDispatcherServicer_to_server(EventDispatcherServicer(), server)
    server.add_insecure_port('[::]:50051')  # Match port used in Java client
    server.start()
    print("gRPC server started on port 50051")
    server.wait_for_termination()

if __name__ == '__main__':
    serve()
