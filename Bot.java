import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

public class Bot {
    public static void main(String[] args) {
        // 환경 변수에서 Together API 키를 가져옵니다.
        String webhookUrl = System.getenv("SLACK_WEBHOOK_URL");
        String LLM_KEY = System.getenv("LLM_KEY");
        String SLACK_WEBHOOK_MESSAGE = System.getenv("SLACK_WEBHOOK_MESSAGE");
        // 요청할 Together API의 엔드포인트 URL
        String togetherUrl = "https://api.together.xyz/v1/chat/completions";

        // 요청 본문(JSON 데이터)
        String jsonData = """
            {
              "model": "meta-llama/Llama-3.3-70B-Instruct-Turbo",
              "messages": [
                {
                  "role": "user",
                  "content": "%s"
                }
              ]
            }
            """.formatted(SLACK_WEBHOOK_MESSAGE);

        // HttpClient 생성
        HttpClient client = HttpClient.newHttpClient();

        // HttpRequest 구성: URL, 헤더, POST 메서드와 본문 설정
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .header("Authorization", "Bearer " + LLM_KEY)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonData))
            .build();

        try {
            // 요청 전송 및 응답 받기
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("응답 상태 코드: " + response.statusCode());
            System.out.println("응답 본문: " + response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
