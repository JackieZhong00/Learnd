import grpc
from concurrent import futures
#should contain message classes
from proto import event_dispatch_pb2 as event_dispatcher_pb2
#should contain stub and servicer classes 
from proto import event_dispatch_pb2_grpc as event_dispatcher_pb2_grpc
from rag import *
from answer_grader_rag import compile_answer_grader, app, AnswerGraphState

#this is grpc object that handles incoming grpc requests 
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



class FlashcardToGradeDispatcherServicer(event_dispatcher_pb2_grpc.FlashcardToGradeDispatcherServicer):
    def DispatchFlashcardToGrade(self, request, context):
        print("Received FlashcardToGrade request:")
        print(request)
        generatedGrade = compile_answer_grader(request)
        print("result from DispatchFlashcardToGrade: ", generatedGrade)
        print("type of grade: ", type(generatedGrade))
        try:

            return event_dispatcher_pb2.FlashcardGrade(grade = generatedGrade)
        except Exception as e:
            context.set_code(grpc.StatusCode.INTERNAL)
            context.set_details(str(e))
            raise

def warmup():
    dummy = AnswerGraphState(
        userId = 0,
        category = "",
        question = "",
        answer = "",
        grade = 0,
        warmup = True,
        needsIndexing = False,
        flashcardId = 0,
        facts = ""
    )
    app.invoke(dummy)

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    event_dispatcher_pb2_grpc.add_EventDispatcherServicer_to_server(EventDispatcherServicer(), server)
    event_dispatcher_pb2_grpc.add_FlashcardToGradeDispatcherServicer_to_server(FlashcardToGradeDispatcherServicer(), server)
    server.add_insecure_port('[::]:50051')  # Match port used in Java client
    server.start()
    print("gRPC server started on port 50051")
    server.wait_for_termination()

#this function runs if this file is executed directly
if __name__ == '__main__':
    warmup()
    serve()
