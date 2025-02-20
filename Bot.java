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
            .uri(URI.create(togetherUrl))
            .header("Authorization", "Bearer " + LLM_KEY)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonData))
            .build();

        try {
            // Together AI에 요청 전송 및 응답 수신
            HttpResponse<String> togetherResponse = client.send(togetherRequest, HttpResponse.BodyHandlers.ofString());
            String responseBody = togetherResponse.body();
            System.out.println("Together API 응답: " + responseBody);
            
            // 3. 응답에서 답변 텍스트 추출 (간단한 파싱)
            // 가정: 응답 JSON 내에 "content"라는 필드가 존재하며, 그 값이 assistant의 답변입니다.
            String assistantMessage = "";
            int contentIndex = responseBody.indexOf("\"content\":\"");
            if (contentIndex != -1) {
                contentIndex += 10; // "content":" 이후의 인덱스
                int endIndex = responseBody.indexOf("\"", contentIndex);
                if (endIndex != -1) {
                    assistantMessage = responseBody.substring(contentIndex, endIndex);
                    assistantMessage = assistantMessage.replace("\\\"", "\"");
                } else {
                    System.err.println("Error: Together API 응답에서 content 끝 인덱스를 찾을 수 없습니다.");
                    return;
                }
            } else {
                System.err.println("Error: Together API 응답에서 'content' 필드를 찾을 수 없습니다.");
                return;
            }
            
            // 4. Slack에 답변 전송 (Slack 웹훅은 { "text": "메시지" } 형식을 요구)
            String slackJson = """
                {
                  "text": "%s"
                }
                """.formatted(assistantMessage);
            
            HttpRequest slackRequest = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(slackJson))
                .build();
            
            HttpResponse<String> slackResponse = client.send(slackRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Slack 응답 상태 코드: " + slackResponse.statusCode());
            System.out.println("Slack 응답 본문: " + slackResponse.body());
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
