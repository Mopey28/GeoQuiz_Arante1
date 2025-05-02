package com.example.geoquiz_arante1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button mTrueButton;
    private Button mFalseButton;
    private Button mNextButton;
    private Button mPrevButton;
    private TextView mQuestionTextView;
    private Button mCheatButton;
    private TextView mCheatCountTextView;
    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_SCORE = "score";
    private static final String KEY_ANSWERED = "answered";
    private static final String KEY_CHEATERS = "cheaters";
    private static final String KEY_CHEAT_COUNT = "cheat_count";
    private static final int REQUEST_CODE_CHEAT = 0;
    private static final int MAX_CHEATS = 3;
    private boolean[] mIsCheater;
    private int mCheatCount = 0;

    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
    };

    private int mCurrentIndex = 0;
    private int mScore = 0;
    private boolean[] mIsAnswered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mScore = savedInstanceState.getInt(KEY_SCORE, 0);
            mIsAnswered = savedInstanceState.getBooleanArray(KEY_ANSWERED);
            mIsCheater = savedInstanceState.getBooleanArray(KEY_CHEATERS);
            mCheatCount = savedInstanceState.getInt(KEY_CHEAT_COUNT, 0);
        } else {
            mIsAnswered = new boolean[mQuestionBank.length];
            mIsCheater = new boolean[mQuestionBank.length];
        }

        mQuestionTextView = findViewById(R.id.question_text_view);
        mCheatCountTextView = findViewById(R.id.cheat_count_text_view);

        mTrueButton = findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(v -> checkAnswer(true));

        mFalseButton = findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(v -> checkAnswer(false));

        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(v -> {
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
            updateQuestion();
        });

        mPrevButton = findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(v -> {
            if (mCurrentIndex > 0) {
                mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
                updateQuestion();
            }
        });

        mCheatButton = findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(v -> {
            if (mCheatCount < MAX_CHEATS) {
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(MainActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            } else {
                Toast.makeText(this, "No more cheats allowed!", Toast.LENGTH_SHORT).show();
            }
        });

        updateQuestion();
        updateCheatCount();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            mIsCheater[mCurrentIndex] = CheatActivity.wasAnswerShown(data);
            mCheatCount++;
            updateCheatCount();
            updateQuestion(); // Update the question to disable buttons if cheated
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
        updateQuestion(); // Ensure the question state is updated on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putInt(KEY_SCORE, mScore);
        savedInstanceState.putBooleanArray(KEY_ANSWERED, mIsAnswered);
        savedInstanceState.putBooleanArray(KEY_CHEATERS, mIsCheater);
        savedInstanceState.putInt(KEY_CHEAT_COUNT, mCheatCount);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentIndex = savedInstanceState.getInt(KEY_INDEX);
        mScore = savedInstanceState.getInt(KEY_SCORE);
        mIsAnswered = savedInstanceState.getBooleanArray(KEY_ANSWERED);
        mIsCheater = savedInstanceState.getBooleanArray(KEY_CHEATERS);
        mCheatCount = savedInstanceState.getInt(KEY_CHEAT_COUNT);
        updateQuestion();
        updateCheatCount();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);

        if (mIsAnswered[mCurrentIndex] || mIsCheater[mCurrentIndex]) {
            mTrueButton.setEnabled(false);
            mFalseButton.setEnabled(false);
            if (mIsCheater[mCurrentIndex]) {
                Toast.makeText(this, "Cheater is wrong!", Toast.LENGTH_SHORT).show();
            }
        } else {
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
        }
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        if (!mIsAnswered[mCurrentIndex]) {
            if (mIsCheater[mCurrentIndex]) {
                Toast.makeText(this, "Cheater is wrong!", Toast.LENGTH_SHORT).show();
            } else {
                if (userPressedTrue == answerIsTrue) {
                    mScore++;
                }

                mIsAnswered[mCurrentIndex] = true;

                mTrueButton.setEnabled(false);
                mFalseButton.setEnabled(false);

                boolean allAnswered = true;
                for (boolean answered : mIsAnswered) {
                    if (!answered) {
                        allAnswered = false;
                        break;
                    }
                }

                if (allAnswered) {
                    double percentage = ((double) mScore / mQuestionBank.length) * 100;
                    Toast.makeText(this, "Score: " + percentage + "%", Toast.LENGTH_LONG).show();
                } else {
                    int messageResId = userPressedTrue == answerIsTrue ? R.string.correct_toast : R.string.incorrect_toast;
                    Toast toast = Toast.makeText(MainActivity.this, messageResId, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                }
            }
        }
    }

    private void updateCheatCount() {
        int remainingCheats = MAX_CHEATS - mCheatCount;
        mCheatCountTextView.setText("Cheats remaining: " + remainingCheats);
        if (mCheatCount >= MAX_CHEATS) {
            mCheatButton.setEnabled(false);
        }
    }
}
