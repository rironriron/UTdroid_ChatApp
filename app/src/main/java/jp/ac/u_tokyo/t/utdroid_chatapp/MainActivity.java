package jp.ac.u_tokyo.t.utdroid_chatapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    /* Viewを格納する変数 */
    private EditText editTextName;
    private EditText editTextMessage;
    private Button buttonSubmit;
    private Button buttonLoad;
    private ListView listView;

    private AQuery aQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* AQueryの初期化 */
        aQuery = new AQuery(this);

        /* それぞれの名前に対応するViewを取得する */
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonLoad = (Button) findViewById(R.id.buttonLoad);
        listView = (ListView) findViewById(R.id.listView);

        /* クリックした時の動作を指定する */
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String message = editTextMessage.getText().toString();
                if (name.isEmpty()) {
                    Toast.makeText(MainActivity.this, "名前を入力して下さい。", Toast.LENGTH_SHORT).show();
                } else if (message.isEmpty()) {
                    Toast.makeText(MainActivity.this, "メッセージを入力して下さい。", Toast.LENGTH_SHORT).show();
                } else {
                    /* メッセージを送信するメソッドを呼び出す */
                    submitMessage(name, message);
                }
            }
        });

        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* サーバからメッセージを読み込むメソッドを呼び出す */
                loadMessage();
            }
        });

        /* ListViewの項目をクリックした時の動作 */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // アイテムの取得
                MessageAdapter adapter = (MessageAdapter) parent.getAdapter();
                Message message = adapter.getItem(position);
                // 内容の表示
                Toast.makeText(MainActivity.this, message.name + "：" + message.message, Toast.LENGTH_SHORT).show();
            }
        });

        // アプリ起動時に1回、メッセージを読み込む
        loadMessage();
    }

    private void loadMessage() {
        submitMessage(null, null);
    }

    private void submitMessage(String name, String message) {
        /* 読み込み完了時のコールバック（ここではJSONObjectの例を示したが、JSONArrayやStringも指定可能） */
        AjaxCallback<JSONObject> callback = new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject root, AjaxStatus status) {
                /* HTTPステータスコードが200なら */
                if (status.getCode() == HttpStatus.SC_OK) {
                    JSONArray array = root.optJSONArray("chat");
                    ArrayList<Message> messageList = Message.parse(array);
                    MessageAdapter adapter = new MessageAdapter(MainActivity.this, messageList);
                    listView.setAdapter(adapter);
                }
            }
        };
        /* URLとメソッドをセット */
        callback.url("https://script.google.com/macros/s/AKfycbwhWErm3SabXhjbg0FqihxEQifhNUYJt6HS-oXhopKMKrhGZG8/exec");
        callback.method(AQuery.METHOD_GET);

        /* パラメータをセット */
        if (name != null && message != null) {
            callback.param("name", name);
            callback.param("message", message);
        }

        /* コールバックの型 */
        callback.type(JSONObject.class);
        /* プログレスダイアログを指定 */
        callback.progress(new ProgressDialog(this));
        /* HTTP通信の実行 */
        aQuery.ajax(callback);
    }
}
