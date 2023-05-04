import static java.util.Calendar.MILLISECOND;

import android.content.Intent;
import androidx.annotation.Nullable;
import com.google.firebase.crashlytics.buildtools.reloc.javax.annotation.Nullable;
import com.google.firebase.database.annotations.Nullable;
import java.util.Calendar;

public class CreateCsv {

    int CREATE_DOCUMENT_REQUEST = 0;
    int RESULT_OK = 1;
    int RESULT_FAILED = 2;

    public void fileOpen(){
        try {
            StringBuffer fileName = new StringBuffer();
            fileName.append(getTime());
            fileName.append(".csv");
            Intent it = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            it.setType("*/*");
            it.putExtra(Intent.EXTRA_TITLE, fileName.toString());
            startActivityForResult(it, CREATE_DOCUMENT_REQUEST);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private String getTime(){
        return Integer.toString(MILLISECOND);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        /* 「CREATE_DOCUMENT_REQUEST」は、Privateで予め定義しておく。 */

        if (requestCode == CREATE_DOCUMENT_REQUEST) {
            //エクスポート
            if (resultCode == RESULT_OK) {
                Uri create_file = data.getData();  //取得した保存先のパスなど。

                //出力処理を実行。その際の引数に上記のUri変数をセットする。
                if (exportCsv_for_SAF(create_file)) {
                    //出力に成功した時の処理。
                } else {
                    //出力に失敗した時の処理。
                }
            } else if (resultCode == RESULT_FAILED) {
                //そもそもアクセスに失敗したなど、保存処理の前に失敗した時の処理。
            }
        }

        //リストの再読み込み
        this.LoadData();

        super.onActivityResult(requestCode, resultCode, data);
    }





}
