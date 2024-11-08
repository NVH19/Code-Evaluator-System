package server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.compiler.CppCompilerRunner;
import server.compiler.JavaCompilerRunner;
import server.compiler.PyCompilerRunner;
import server.models.TestCase;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.out;

public class Server {
    private static final int PORT = 2024;
    private static final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private static final String EXERCISES_FILE_PATH = "Server/src/main/java/server/exercises.json";
    private static final String HISTORIES_FILE_PATH = "Server/src/main/java/server/data/Histories.txt";
    private static final String USER_FILE_PATH = "Server/src/main/java/server/data/User.txt";
    private static final ExecutorService clientThreadPool = Executors.newFixedThreadPool(100);
    private static final JavaCompilerRunner javaCompilerRunner = new JavaCompilerRunner();
    private static final CppCompilerRunner cppCompilerRunner = new CppCompilerRunner();
    private static final PyCompilerRunner pyCompilerRunner  = new PyCompilerRunner();
    // Sending exam start time and duration
    static LocalTime examStartTime = LocalTime.of(10, 10); // Change as needed
    static int examDuration = 60 * 60;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                out.println("New client connected from IP: " + socket.getInetAddress().getHostAddress());
                // Create and start a handler thread for each client
                ClientHandler clientHandler = new ClientHandler(socket);
                clientThreadPool.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private String userId;
        private String exerciseId;
        private boolean loggedIn = false;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try (BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String request;
                while (true) {  // Vòng lặp vô hạn để xử lý các yêu cầu
                    request = in.readLine();  // Đọc yêu cầu từ client
                    System.out.println("Received request: " + request);

                    if (request.startsWith("LOGIN:")) {
                        String username = request.split(":")[1];
                        if (validateUser(username, out)) {
                            userId = username;
                            clients.put(userId, this);
                            loggedIn = true;
                            out.println("LOGIN_SUCCESS");
                            out.println(examStartTime.toString()); // Send start time
                            out.println(examDuration); // Send duration
                            System.out.println("User " + userId + " has logged in.");
                        } else {
                            out.println("Invalid username or password.");
                        }
                    } else if (loggedIn) { // Chỉ xử lý các yêu cầu khác khi đã đăng nhập thành công
                        handleClientRequest(request, in, out, bis);
                    } else {
                        out.println("Please login first.");
                    }
                }

            } catch (SocketException e) {
                System.err.println("Client disconnected abruptly: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Connection error with user " + userId + ": " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Chỉ đóng socket nếu user đã gửi yêu cầu "LOGOUT" hoặc nếu kết nối bị gián đoạn.
                if (userId != null && clients.containsKey(userId)) {
                    clients.remove(userId);
                }
                if (!socket.isClosed()) {  // Chỉ đóng khi socket vẫn mở
                    try {
                        socket.close();
                        out.println("Socket closed gracefully.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        private void handleClientRequest(String request, BufferedReader in, PrintWriter out, BufferedInputStream bis) throws IOException {
            System.out.println("Handling request: " + request);
            System.out.println("User ID: " + userId);
            switch (request.toUpperCase()) {
                case "GET_EXERCISES":
                    JSONArray exercisesArray = loadExercisesFromFile(EXERCISES_FILE_PATH);
                    out.println(exercisesArray != null ? exercisesArray.toJSONString() : "[]");
                    break;

                case "SEND_FILE":
                    out.println("READY_TO_RECEIVE_FILE");
                    receiveFile(bis, in, out);
                    System.out.println("File received successfully.");
//                    out.println("File received successfully.");
                    break;

                case "GET_HISTORY":
                    JSONArray historiesArray = getUserHistory(userId);
                    out.println(historiesArray.toJSONString());
                    break;

                case "GET_RANKING":
                    JSONArray rankingArray = getRankingList(); // Call the new method to get ranking
                    out.println(rankingArray.toJSONString()); // Send the ranking list to the client
                    break;

                case "START_EXAM":
                    out.println("EXAM_STARTED");
                    break;

                case "CLEAR_HISTORY":
                    clearUserHistoryFromFile(userId);
                    out.println("HISTORY_CLEARED");
                    break;

                case "FETCH_EXAM_END_TIME": // New case for fetching the exam end time
                    System.out.println("Sending exam end time to client...");
                    out.println(fetchServerExamEndTime()); // Send the calculated end time
                    break;

                default:
                    out.println("Invalid request: " + request);
                    break;
            }
        }

        private boolean validateUser(String username, PrintWriter out) {
            try (BufferedReader userReader = new BufferedReader(new FileReader(USER_FILE_PATH))) {
                String line;
                userReader.readLine(); // Skip header
                while ((line = userReader.readLine()) != null) {
                    String[] userData = line.split(" - ");
                    if (userData[0].equals(username)) {
                        // For simplicity, assuming the password is verified here
                        return true; // Implement actual password checking
                    }
                }
            } catch (IOException e) {
                out.println("User data file not found: " + e.getMessage());
            }
            return false;
        }

        private String fetchServerExamEndTime() {
            // Calculate the exam end time based on start time and duration
            LocalDateTime examStartDateTime = LocalDateTime.now().with(LocalTime.of(examStartTime.getHour(), examStartTime.getMinute()));
            LocalDateTime examEndDateTime = examStartDateTime.plusSeconds(examDuration);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            return examEndDateTime.format(formatter);
        }

        private JSONArray getUserHistory(String userId) {
            JSONArray userHistoryArray = new JSONArray();
            try (BufferedReader reader = new BufferedReader(new FileReader(HISTORIES_FILE_PATH))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(userId)) { // Kiểm tra dòng bắt đầu bằng userId
                        String[] parts = line.split(" - ");
                        if (parts.length == 4) { // Kiểm tra số lượng phần
                            JSONObject history = new JSONObject();
                            history.put("idSv", parts[0]);           // userId
                            history.put("idExercises", parts[1]);    // exerciseId
                            history.put("status", parts[2]);         // status
                            history.put("dateTime", parts[3]);       // dateTime
                            userHistoryArray.add(history);            // Thêm lịch sử vào mảng
                        } else {
                            System.err.println("Invalid history line: " + line);
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return userHistoryArray; // Trả về lịch sử của người dùng
        }

        public JSONArray getRankingList() {
            JSONArray rankingArray = new JSONArray();
            try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE_PATH))) {
                String line;
                reader.readLine(); // Skip header
                while ((line = reader.readLine()) != null) {
                    // Example line format: "userId - userName - correctAnswers - rank"
                    String[] parts = line.split(" - ");

                    if (parts.length == 4) { // Ensure there are exactly 4 parts
                        JSONObject ranking = new JSONObject();
                        ranking.put("idSv", parts[0]); // userId
                        ranking.put("name", parts[1]);  // userName
                        // Safely parse the number of correct answers and rank
                        try {
                            int correctAnswers = Integer.parseInt(parts[2]); // Number of correct answers
                            int rank = Integer.parseInt(parts[3]); // Rank
                            ranking.put("answerCorrectNumber", correctAnswers);
                            ranking.put("rank", rank);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid number format in line: " + line); // Log the error
                            continue; // Skip this entry if parsing fails
                        }

                        rankingArray.add(ranking); // Add ranking to the array
                    } else {
                        System.err.println("Invalid ranking line: " + line);
                    }
                }
            } catch (IOException | NumberFormatException | JSONException e) {
                e.printStackTrace();
            }
            return rankingArray; // Return the ranking data
        }


        private void receiveFile(BufferedInputStream bis, BufferedReader reader, PrintWriter out) throws IOException {
            System.out.println("Receiving file from client...");

            // Đọc mã bài tập từ client
            String exerciseId = reader.readLine(); // Nhận mã bài tập
            System.out.println("Mã bài tập nhận được: " + exerciseId); // In mã bài tập ra console

            String fileName = reader.readLine(); // Đọc tên file
            System.out.println("Tên file nhận được: " + fileName); // In tên file ra console

            // Tạo đường dẫn để lưu tệp tin
            String saveFilePath = "Server/src/main/java/server/received_files/" + fileName; // Đường dẫn lưu tệp
            System.out.println("Bắt đầu lưu file tại: " + saveFilePath); // Thêm dòng này

            // Tạo FileOutputStream để ghi tệp
            try (FileOutputStream fos = new FileOutputStream(saveFilePath)) {
                byte[] buffer = new byte[4096];
                StringBuilder fileData = new StringBuilder(); // Bộ đệm cho toàn bộ nội dung file
                int bytesRead;

                while ((bytesRead = bis.read(buffer)) != -1) {
                    // Chuyển dữ liệu vừa đọc thành chuỗi
                    String chunk = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                    fileData.append(chunk); // Thêm phần đọc được vào chuỗi dữ liệu file

                    // Kiểm tra nếu gặp "END_OF_FILE" thì dừng
                    if (fileData.toString().contains("END_OF_FILE")) {
                        break;
                    }
                }

                // Loại bỏ "END_OF_FILE" và ghi dữ liệu còn lại vào file
                String finalFileContent = fileData.toString().replace("END_OF_FILE", "");
                fos.write(finalFileContent.getBytes(StandardCharsets.UTF_8));
                fos.flush(); // Đảm bảo tất cả dữ liệu đã được ghi
                System.out.println("File đã được lưu tại: " + saveFilePath);

                int successCount = 0;
                // Đọc test cases từ JSON và chạy class
                List<TestCase> testCases = readTestCasesFromJSON(exerciseId);
                // Biên dịch file nếu là file Java
                if (fileName.endsWith(".java")) {
                    javaCompilerRunner.compileJavaFile(saveFilePath); // Biên dịch file Java

                    // Tạo tên class từ tên file Java
                    String className = fileName.substring(0, fileName.lastIndexOf('.'));
                    System.out.println("Tên class: " + className);

                    successCount = javaCompilerRunner.runJavaClass(className, exerciseId, testCases);
                }
                // Biên dịch file nếu là file C++
                if (fileName.endsWith(".cpp")) {
                    cppCompilerRunner.compileCppFile(saveFilePath); // Biên dịch file C++

                    // Đọc test cases từ JSON và chạy class
                    successCount = cppCompilerRunner.runCppExecutable("exercise_id", testCases);
                }
                if (fileName.endsWith(".py")){
                    // Đọc test cases từ JSON và chạy class
                    successCount = pyCompilerRunner.runPythonScript(saveFilePath,"exercise_id", testCases);
                }

                // Xử lý kết quả sau khi chạy test cases
                appendToHistoryFile(userId, exerciseId, successCount == testCases.size() ? "AC" : "WA");
                // Gửi kết quả cho client
                sendResultToClient(successCount, testCases.size(), out);
            } catch (IOException e) {
                System.err.println("Lỗi khi lưu file: " + e.getMessage());
            }
        }

        // Gửi thông báo thành công hay thất bại tới client
        private void sendResultToClient(int successCount, int totalTests, PrintWriter out) {

            if (successCount == totalTests) {
                out.println("SUCCESS: All test cases passed.");
            } else {
                out.println("FAILURE: " + successCount + "/" + totalTests + " test case(s) passed.");
            }
        }

        private List<TestCase> readTestCasesFromJSON(String exerciseId) {
            List<TestCase> testCases = new ArrayList<>();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode exercises = objectMapper.readTree(new File(EXERCISES_FILE_PATH));

                for (JsonNode exercise : exercises) {
                    if (exercise.get("exerciseId").asText().equals(exerciseId)) {
                        JsonNode cases = exercise.get("testCases");
                        for (JsonNode testCase : cases) {
                            String input = testCase.get("input").asText();
                            String expectedOutput = testCase.get("expectedOutput").asText();
                            testCases.add(new TestCase(input, expectedOutput));
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return testCases;
        }

        private void appendToHistoryFile(String userId, String exerciseId, String status) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String dateTime = now.format(formatter);
            String historyEntry = userId + " - " + exerciseId + " - " + status + " - " + dateTime + "\n";

            try (BufferedReader reader = new BufferedReader(new FileReader(HISTORIES_FILE_PATH));
                 FileWriter fw = new FileWriter(HISTORIES_FILE_PATH, true)) {

                // Check if the user has already submitted a correct answer for this exercise
                String line;
                boolean alreadySubmittedCorrectly = false;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(userId + " - " + exerciseId + " - AC")) {
                        alreadySubmittedCorrectly = true;
                        break;
                    }
                }

                fw.write(historyEntry);
                out.println("History updated for user: " + userId);
                if (status.equals("AC") && !alreadySubmittedCorrectly) {
                    // Update the user's correct answers count if it's a new correct submission
                    updateCorrectAnswersCount(userId);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void updateCorrectAnswersCount(String userId) {
            File userFile = new File(USER_FILE_PATH);
            File tempFile = new File(USER_FILE_PATH + ".tmp");

            try (BufferedReader reader = new BufferedReader(new FileReader(userFile));
                 PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip the header line
                    if (line.startsWith("userId")) {
                        writer.println(line);
                        continue;
                    }

                    String[] parts = line.split(" - ");
                    if (parts[0].equals(userId)) {
                        // Increment the correct answers count
                        int correctAnswers = Integer.parseInt(parts[2]);
                        correctAnswers++; // Increase the correct answer count
                        parts[2] = String.valueOf(correctAnswers); // Update the correct answers count

                        // Write the updated line back to the temporary file
                        writer.println(String.join(" - ", parts));
                    } else {
                        // Write the original line back to the temporary file
                        writer.println(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Replace the original user file with the updated temporary file
            if (!userFile.delete()) {
                System.err.println("Could not delete the original user file");
                return;
            }
            if (!tempFile.renameTo(userFile)) {
                System.err.println("Could not rename temporary file to user file");
            }
        }

        // Hàm xóa lịch sử của người dùng sau khi hết giờ
        public void clearUserHistoryFromFile(String userID) {
            File inputFile = new File(HISTORIES_FILE_PATH);
            File tempFile = new File(HISTORIES_FILE_PATH + ".tmp");

            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith(userID + " ")) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!inputFile.delete()) {
                System.err.println("Could not delete the original history file");
                return;
            }
            if (!tempFile.renameTo(inputFile)) {
                System.err.println("Could not rename temporary file to history file");
            }
        }

        private JSONArray loadExercisesFromFile(String filePath) {
            JSONParser parser = new JSONParser();
            try (FileReader reader = new FileReader(filePath)) {
                return (JSONArray) parser.parse(reader);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
