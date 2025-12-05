import grpc
from concurrent import futures
import proto.event_dispatcher_pb2 as event_dispatcher_pb2
import proto.event_dispatcher_pb2_grpc as event_dispatcher_pb2_grpc
from rag import *
from answer_grader_rag import *

#this is the grpc object that handles incoming grpc requests 
class EventDispatcherServicer(event_dispatcher_pb2_grpc.EventDispatcherServicer):
    def __init__ (self):
        self.recommender = compile_graph
        self.answer_grader = compile_answer_grader
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

#this function runs if this file is executed directly
if __name__ == '__main__':
    serve()
