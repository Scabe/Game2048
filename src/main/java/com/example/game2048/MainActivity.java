package com.example.game2048;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements OnGame2048Listener{

    private Game2048Layout mGame2048Layout;
    private TextView mScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mScore = findViewById(R.id.id_score);
        mGame2048Layout = findViewById(R.id.id_game2048);
        mGame2048Layout.setOnGame2048Listener(this);
    }

    @Override
    public void onScoreChange(int score) {
        mScore.setText("SCORE:"+ score);
    }

    @Override
    public void onGameOver() {
        new AlertDialog.Builder(this).setTitle("GAME OVER").setMessage("YOU HAVE GOT"+mScore.getText()).setPositiveButton("EXIT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).show();
    }
}
