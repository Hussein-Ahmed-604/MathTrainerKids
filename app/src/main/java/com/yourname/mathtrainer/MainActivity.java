package com.yourname.mathtrainer;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // المتغيرات الأساسية
    private int currentLevel = 1;
    private int points = 0;
    private int hearts = 3;
    private int currentQuestion = 1;
    private int totalQuestionsInLevel = 5;
    private boolean gameActive = true;

    // أسماء المستويات الروسية
    private final String[] levelNames = {
            "Начинающий", "Ученик", "Опытный", "Мастер", "Эксперт",
            "Гений", "Волшебник", "Профессор", "Легенда", "Бог математики"
    };

    // عدد الأسئلة لكل مستوى
    private final int[] questionsPerLevel = {5, 5, 5, 6, 6, 7, 7, 8, 8, 10};

    // الوقت لكل مستوى
    private final int[] levelTimeLimits = {45, 40, 35, 30, 25, 20, 18, 15, 12, 10};

    // عناصر الواجهة
    private TextView textLevel, textPoints, textQuestion, textTimer, textQuestionCounter;
    private TextView txtResult;
    private EditText editAnswer;
    private Button buttonCheck;
    private LinearLayout heartsContainer;
    private CountDownTimer timer;
    private int timeLeft;
    private final Random random = new Random();

    // فواكه إيموجي
    private final String[] fruits = {"🍎", "🍐", "🍊", "🍌", "🍓", "🍇", "🍒", "🍑"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        startNewLevel();
    }

    private void initializeViews() {
        textLevel = findViewById(R.id.textLevel);
        textPoints = findViewById(R.id.textPoints);
        textQuestion = findViewById(R.id.textQuestion);
        textTimer = findViewById(R.id.textTimer);
        textQuestionCounter = findViewById(R.id.textQuestionCounter);
        editAnswer = findViewById(R.id.editAnswer);
        buttonCheck = findViewById(R.id.buttonCheck);
        heartsContainer = findViewById(R.id.heartsContainer);
        txtResult = findViewById(R.id.txtResult);

        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameActive) {
                    checkAnswer();
                }
            }
        });
    }

    private void startNewLevel() {
        gameActive = true;
        currentQuestion = 1;
        totalQuestionsInLevel = questionsPerLevel[currentLevel - 1];
        timeLeft = levelTimeLimits[currentLevel - 1];

        updateLevelDisplay();
        generateQuestion();
        startTimer();
        updateQuestionCounter();
        hideResultMessage();
    }

    private void updateLevelDisplay() {
        textLevel.setText("Уровень " + currentLevel + ": " + levelNames[currentLevel - 1]);
        textPoints.setText("Очки: " + points);
        updateHeartsDisplay();
    }

    private void updateHeartsDisplay() {
        heartsContainer.removeAllViews();

        for (int i = 0; i < hearts; i++) {
            TextView heart = new TextView(this);
            heart.setText("❤️");
            heart.setTextSize(24);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(5, 0, 5, 0);
            heart.setLayoutParams(params);
            heartsContainer.addView(heart);
        }

        if (hearts == 0) {
            TextView emptyHeart = new TextView(this);
            emptyHeart.setText("💔");
            emptyHeart.setTextSize(24);
            heartsContainer.addView(emptyHeart);
        }
    }

    private void generateQuestion() {
        int questionType;

        // تحديد نوع السؤال بناءً على المستوى
        if (currentLevel <= 3) {
            // المستويات 1-3: فواكه فقط، جمع بسيط
            questionType = 0;
        } else if (currentLevel <= 6) {
            // المستويات 4-6: فواكه مع عمليات متعددة أو أرقام
            questionType = random.nextInt(2); // 0 أو 1
        } else {
            // المستويات 7-10: جميع الأنواع
            questionType = random.nextInt(3); // 0 أو 1 أو 2
        }

        String questionText;
        int answer;

        switch (questionType) {
            case 0:
                // نوع 1: فواكه فقط مع عمليات متعددة
                questionText = generateFruitQuestionWithMultipleOperations();
                answer = calculateFruitQuestionAnswer(questionText);
                break;

            case 1:
                // نوع 2: أرقام فقط (بدون فواكه)
                questionText = generateNumberQuestion();
                answer = calculateNumberQuestionAnswer(questionText);
                break;

            default:
                // نوع 3: خليط (سؤال فواكه وسؤال أرقام في نفس الواجهة)
                questionText = generateMixedQuestions();
                answer = calculateMixedQuestionsAnswer(questionText);
                break;
        }

        buttonCheck.setTag(answer);
        textQuestion.setText(questionText);
        editAnswer.setText("");
        editAnswer.requestFocus();
    }

    // ========== الأنواع الجديدة من الأسئلة ==========

    // النوع 1: فواكه مع عمليات متعددة (+, - في نفس المسألة)
    private String generateFruitQuestionWithMultipleOperations() {
        StringBuilder question = new StringBuilder();
        int totalAnswer = 0;

        // عدد العمليات: يزيد مع المستوى
        int operationsCount = 2 + (currentLevel / 3); // 2-5 عمليات

        String currentFruit = fruits[random.nextInt(fruits.length)];
        int currentNumber = random.nextInt(5) + 1 + currentLevel;

        question.append(getFruitString(currentFruit, currentNumber)).append(" ");
        totalAnswer = currentNumber;

        for (int i = 1; i < operationsCount; i++) {
            String operation;
            if (currentLevel <= 4) {
                operation = "+";
            } else {
                // بعد المستوى 4: ممكن + أو -
                operation = random.nextBoolean() ? "+" : "-";
            }

            String nextFruit = fruits[random.nextInt(fruits.length)];
            // لتجنب نفس الفاكهة متتالية
            while (nextFruit.equals(currentFruit) && random.nextBoolean()) {
                nextFruit = fruits[random.nextInt(fruits.length)];
            }

            int nextNumber = random.nextInt(5) + 1 + (currentLevel / 2);

            question.append(operation).append(" ")
                    .append(getFruitString(nextFruit, nextNumber)).append(" ");

            if (operation.equals("+")) {
                totalAnswer += nextNumber;
            } else {
                totalAnswer -= nextNumber;
                // تأكد أن النتيجة لا تكون سالبة
                if (totalAnswer < 0) {
                    totalAnswer = 0;
                }
            }

            currentFruit = nextFruit;
        }

        question.append("= ?");
        buttonCheck.setTag(totalAnswer);
        return question.toString();
    }

    // النوع 2: أرقام فقط (بدون فواكه)
    private String generateNumberQuestion() {
        StringBuilder question = new StringBuilder();
        int totalAnswer = 0;

        int operationsCount = 2 + (currentLevel / 4); // 2-4 عمليات

        int currentNumber = random.nextInt(10) + 1 + currentLevel;
        question.append(currentNumber).append(" ");
        totalAnswer = currentNumber;

        for (int i = 1; i < operationsCount; i++) {
            String operation;
            if (currentLevel <= 5) {
                operation = "+";
            } else if (currentLevel <= 8) {
                operation = random.nextBoolean() ? "+" : "-";
            } else {
                // المستوى 9-10: +, -, ×
                int opType = random.nextInt(3);
                operation = (opType == 0) ? "+" : (opType == 1) ? "-" : "×";
            }

            int nextNumber = random.nextInt(10) + 1 + (currentLevel / 2);

            question.append(operation).append(" ").append(nextNumber).append(" ");

            switch (operation) {
                case "+":
                    totalAnswer += nextNumber;
                    break;
                case "-":
                    totalAnswer -= nextNumber;
                    if (totalAnswer < 0) totalAnswer = 0;
                    break;
                case "×":
                    totalAnswer *= nextNumber;
                    break;
            }
        }

        question.append("= ?");
        buttonCheck.setTag(totalAnswer);
        return question.toString();
    }

    // النوع 3: أسئلة مختلطة (سؤال فواكه وسؤال أرقام منفصلين)
    private String generateMixedQuestions() {
        StringBuilder question = new StringBuilder();

        // سؤال الفواكه
        String fruitQuestion = generateSimpleFruitQuestion();
        int fruitAnswer = calculateSimpleFruitAnswer(fruitQuestion);

        // سؤال الأرقام
        String numberQuestion = generateSimpleNumberQuestion();
        int numberAnswer = calculateSimpleNumberAnswer(numberQuestion);

        // مجموع الإجابتين
        int totalAnswer = fruitAnswer + numberAnswer;

        question.append("🍎 Задача 1: ").append(fruitQuestion)
                .append("\n\n")
                .append("🔢 Задача 2: ").append(numberQuestion)
                .append("\n\n")
                .append("📊 Общая сумма = ?");

        buttonCheck.setTag(totalAnswer);
        return question.toString();
    }

    // ========== دوال المساعدة ==========

    private String getFruitString(String fruit, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(fruit).append(" ");
        }
        return sb.toString().trim();
    }

    private String generateSimpleFruitQuestion() {
        String fruit1 = fruits[random.nextInt(fruits.length)];
        String fruit2 = fruits[random.nextInt(fruits.length)];

        int num1 = random.nextInt(5) + 1 + currentLevel;
        int num2 = random.nextInt(5) + 1;
        String operation = random.nextBoolean() ? "+" : "-";

        return getFruitString(fruit1, num1) + " " + operation + " " + getFruitString(fruit2, num2) + " = ?";
    }

    private int calculateSimpleFruitAnswer(String question) {
        // تحليل سؤال الفواكه البسيط
        String[] parts = question.split(" ");
        int num1 = parts[0].length() / 2; // تقدير عدد الفواكه
        int num2 = parts[2].length() / 2;
        String operation = parts[1];

        return operation.equals("+") ? num1 + num2 : num1 - num2;
    }

    private String generateSimpleNumberQuestion() {
        int num1 = random.nextInt(15) + 5 + currentLevel;
        int num2 = random.nextInt(10) + 1;
        String operation = random.nextBoolean() ? "+" : "-";

        return num1 + " " + operation + " " + num2 + " = ?";
    }

    private int calculateSimpleNumberAnswer(String question) {
        String[] parts = question.split(" ");
        int num1 = Integer.parseInt(parts[0]);
        int num2 = Integer.parseInt(parts[2]);
        String operation = parts[1];

        return operation.equals("+") ? num1 + num2 : num1 - num2;
    }

    private int calculateFruitQuestionAnswer(String question) {
        return (int) buttonCheck.getTag(); // تم حسابها مسبقاً
    }

    private int calculateNumberQuestionAnswer(String question) {
        return (int) buttonCheck.getTag(); // تم حسابها مسبقاً
    }

    private int calculateMixedQuestionsAnswer(String question) {
        return (int) buttonCheck.getTag(); // تم حسابها مسبقاً
    }

    // ========== بقية الدوال (نفسها) ==========

    private void checkAnswer() {
        String answerStr = editAnswer.getText().toString().trim();

        if (answerStr.isEmpty()) {
            showResultMessage("Пожалуйста, введите ответ!", false);
            return;
        }

        try {
            int userAnswer = Integer.parseInt(answerStr);
            int correctAnswer = (int) buttonCheck.getTag();

            if (userAnswer == correctAnswer) {
                points += currentLevel * 10;
                textPoints.setText("Очки: " + points);
                showResultMessage("Правильно! 👍", true);

                currentQuestion++;
                if (currentQuestion > totalQuestionsInLevel) {
                    levelComplete();
                } else {
                    updateQuestionCounter();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            generateQuestion();
                            hideResultMessage();
                        }
                    }, 1500);
                }
            } else {
                hearts--;
                updateHeartsDisplay();

                String message = "Неправильно! ❌\nПравильный ответ: " + correctAnswer;
                showResultMessage(message, false);

                if (hearts <= 0) {
                    gameOver();
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            generateQuestion();
                            hideResultMessage();
                        }
                    }, 3000);
                }
            }
        } catch (NumberFormatException e) {
            showResultMessage("Введите число!", false);
        }
    }

    private void showResultMessage(String message, boolean isCorrect) {
        txtResult.setText(message);
        if (isCorrect) {
            txtResult.setTextColor(Color.parseColor("#388E3C"));
            txtResult.setBackgroundColor(Color.parseColor("#E8F5E9"));
        } else {
            txtResult.setTextColor(Color.parseColor("#D32F2F"));
            txtResult.setBackgroundColor(Color.parseColor("#FFEBEE"));
        }
        txtResult.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (gameActive) {
                    hideResultMessage();
                }
            }
        }, 3000);
    }

    private void hideResultMessage() {
        txtResult.setVisibility(View.GONE);
    }

    private void updateQuestionCounter() {
        textQuestionCounter.setText("Вопрос: " + currentQuestion + "/" + totalQuestionsInLevel);
    }

    private void levelComplete() {
        gameActive = false;
        if (timer != null) {
            timer.cancel();
        }

        if (currentLevel < 10) {
            currentLevel++;
            showResultMessage("🎉 Уровень пройден! Переход на уровень " + currentLevel + "!", true);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startNewLevel();
                }
            }, 3000);
        } else {
            gameComplete();
        }
    }

    private void gameComplete() {
        gameActive = false;
        if (timer != null) {
            timer.cancel();
        }

        String message = "🎊 ПОБЕДА! 🎊\n" +
                "Ты прошел все уровни!\n" +
                "Итоговые очки: " + points + "\n" +
                "Ты настоящий математик!";

        textQuestion.setText("🎉 ПОБЕДА! 🎉");
        editAnswer.setEnabled(false);
        buttonCheck.setEnabled(false);
        showResultMessage(message, true);
    }

    private void gameOver() {
        gameActive = false;
        if (timer != null) {
            timer.cancel();
        }

        textQuestion.setText("💔 КОНЕЦ ИГРЫ");
        editAnswer.setEnabled(false);
        buttonCheck.setEnabled(false);

        showResultMessage("Игра окончена! Попробуй еще раз!\nИтоговые очки: " + points, false);

        buttonCheck.setText("ИГРАТЬ СНОВА");
        buttonCheck.setBackgroundColor(Color.parseColor("#F44336"));
        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame();
            }
        });
    }

    private void restartGame() {
        currentLevel = 1;
        points = 0;
        hearts = 3;
        currentQuestion = 1;
        gameActive = true;

        buttonCheck.setText("ПРОВЕРИТЬ");
        buttonCheck.setBackgroundColor(Color.parseColor("#4CAF50"));
        editAnswer.setEnabled(true);

        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameActive) {
                    checkAnswer();
                }
            }
        });

        startNewLevel();
    }

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }

        final int totalTime = timeLeft;

        timer = new CountDownTimer(timeLeft * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = (int) (millisUntilFinished / 1000);
                textTimer.setText(String.valueOf(timeLeft));

                if (timeLeft <= 10) {
                    textTimer.setTextColor(Color.RED);
                } else if (timeLeft <= 20) {
                    textTimer.setTextColor(Color.parseColor("#FF9800"));
                } else {
                    textTimer.setTextColor(Color.parseColor("#1976D2"));
                }
            }

            @Override
            public void onFinish() {
                if (gameActive) {
                    timeOut();
                }
            }
        }.start();
    }

    private void timeOut() {
        hearts--;
        updateHeartsDisplay();

        showResultMessage("Время вышло! ⏰", false);

        if (hearts <= 0) {
            gameOver();
        } else {
            currentQuestion++;
            if (currentQuestion > totalQuestionsInLevel) {
                levelComplete();
            } else {
                updateQuestionCounter();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        generateQuestion();
                        startTimer();
                        hideResultMessage();
                    }
                }, 2000);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}