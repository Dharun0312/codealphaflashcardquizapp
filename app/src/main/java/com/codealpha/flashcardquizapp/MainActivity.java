package com.codealpha.flashcardquizapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends Activity {

    TextView cardText, cardLabel, cardCount;
    Button showAnswerBtn, nextBtn, previousBtn;
    Button addBtn, editBtn, deleteBtn;

    ArrayList<String> questions = new ArrayList<>();
    ArrayList<String> answers = new ArrayList<>();

    int currentIndex = 0;
    boolean answerVisible = false;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardText = findViewById(R.id.cardText);
        cardLabel = findViewById(R.id.cardLabel);
        cardCount = findViewById(R.id.cardCount);

        showAnswerBtn = findViewById(R.id.showAnswerBtn);
        nextBtn = findViewById(R.id.nextBtn);
        previousBtn = findViewById(R.id.previousBtn);
        addBtn = findViewById(R.id.addBtn);
        editBtn = findViewById(R.id.editBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        preferences = getSharedPreferences(
                "FlashcardData", MODE_PRIVATE);

        loadCards();

        if (questions.isEmpty()) {
            addSampleCards();
            saveCards();
        }

        displayCard();

        showAnswerBtn.setOnClickListener(v -> {
            answerVisible = !answerVisible;
            displayCard();
        });

        nextBtn.setOnClickListener(v -> {
            if (!questions.isEmpty()) {
                currentIndex =
                        (currentIndex + 1) % questions.size();
                answerVisible = false;
                displayCard();
            }
        });

        previousBtn.setOnClickListener(v -> {
            if (!questions.isEmpty()) {
                currentIndex =
                        (currentIndex - 1 + questions.size())
                                % questions.size();
                answerVisible = false;
                displayCard();
            }
        });

        addBtn.setOnClickListener(v -> showDialog(false));

        editBtn.setOnClickListener(v -> {
            if (!questions.isEmpty()) {
                showDialog(true);
            }
        });

        deleteBtn.setOnClickListener(v -> deleteCard());
    }

    private void addSampleCards() {
        questions.add("What is Java?");
        answers.add("Java is an object-oriented programming language.");

        questions.add("What is Android Studio?");
        answers.add("Android Studio is the official Android IDE.");

        questions.add("What is XML?");
        answers.add("XML is used to design Android layouts.");

        questions.add("What is SQLite?");
        answers.add("SQLite is a lightweight local database.");

        questions.add("What is an Activity?");
        answers.add("An Activity represents an app screen.");
    }

    private void displayCard() {

        if (questions.isEmpty()) {
            cardText.setText("No flashcards available");
            cardLabel.setText("EMPTY");
            cardCount.setText("0 Cards");
            showAnswerBtn.setVisibility(View.GONE);
            return;
        }

        showAnswerBtn.setVisibility(View.VISIBLE);

        if (answerVisible) {
            cardText.setText(answers.get(currentIndex));
            cardLabel.setText("ANSWER");
            showAnswerBtn.setText("Show Question");
        } else {
            cardText.setText(questions.get(currentIndex));
            cardLabel.setText("QUESTION");
            showAnswerBtn.setText("Show Answer");
        }

        cardCount.setText("Card " + (currentIndex + 1)
                + " of " + questions.size());
    }

    private void showDialog(boolean isEdit) {

        EditText questionInput = new EditText(this);
        questionInput.setHint("Enter question");

        EditText answerInput = new EditText(this);
        answerInput.setHint("Enter answer");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        layout.addView(questionInput);
        layout.addView(answerInput);

        if (isEdit) {
            questionInput.setText(questions.get(currentIndex));
            answerInput.setText(answers.get(currentIndex));
        }

        new AlertDialog.Builder(this)
                .setTitle(isEdit ? "Edit Flashcard" : "Add Flashcard")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {

                    String q = questionInput.getText().toString().trim();
                    String a = answerInput.getText().toString().trim();

                    if (q.isEmpty() || a.isEmpty()) {
                        Toast.makeText(this,
                                "Enter both question and answer",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEdit) {
                        questions.set(currentIndex, q);
                        answers.set(currentIndex, a);
                    } else {
                        questions.add(q);
                        answers.add(a);
                        currentIndex = questions.size() - 1;
                    }

                    answerVisible = false;
                    saveCards();
                    displayCard();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCard() {

        if (questions.isEmpty()) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Flashcard")
                .setMessage("Are you sure you want to delete this card?")
                .setPositiveButton("Delete", (dialog, which) -> {

                    questions.remove(currentIndex);
                    answers.remove(currentIndex);

                    if (currentIndex >= questions.size()
                            && currentIndex > 0) {
                        currentIndex--;
                    }

                    answerVisible = false;
                    saveCards();
                    displayCard();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveCards() {
        try {
            JSONArray array = new JSONArray();

            for (int i = 0; i < questions.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("question", questions.get(i));
                obj.put("answer", answers.get(i));
                array.put(obj);
            }

            preferences.edit()
                    .putString("cards", array.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCards() {
        try {
            String data = preferences.getString("cards", "[]");
            JSONArray array = new JSONArray(data);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                questions.add(obj.getString("question"));
                answers.add(obj.getString("answer"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}