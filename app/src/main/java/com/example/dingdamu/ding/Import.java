package com.example.dingdamu.ding;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by dingdamu on 10/05/16.
 */
public class Import extends AppCompatActivity {
    Uri imageUri;
    int IMAGE_CONST = 1;
    ArrayList<String> sqluri,sqlcoordinate,sqladdress,sqltime;
    PostORM p = new PostORM();
    ArrayList<ArrayList<String>> holder;
    ListView feedList;
    PostAdapter adapter;
    FloatingActionButton add,add2;
    Output op=new Output();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_import);
        sqluri = new ArrayList<>();
        sqladdress = new ArrayList<>();
        sqlcoordinate = new ArrayList<>();
        sqltime = new ArrayList<>();
        feedList = (ListView)findViewById(R.id.feedList);
        holder = new ArrayList<ArrayList<String>>();
        //拍照
        add = (FloatingActionButton)findViewById(R.id.fab1);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File mFile=op.getOutputUri(IMAGE_CONST,Import.this);
                imageUri = Uri.fromFile(mFile);
                if (imageUri == null) {
                    Toast.makeText(Import.this, R.string.storage_access_error, Toast.LENGTH_SHORT).show();
                } else {
                    pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(pictureIntent, IMAGE_CONST);
                }
            }
        });

        add2 = (FloatingActionButton)findViewById(R.id.fab2);
        add2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Import.this, Compass_camera_Activity.class);
                startActivity(intent);
                Import.this.finish();
            }
        });
        new SQLTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_import, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_goback) {
            Intent intent = new Intent();
            intent.setClass(Import.this, MainActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    //populates the list asynchronously
    public class SQLTask extends AsyncTask<String,String,ArrayList<ArrayList<String>>>
    {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Import.this);
            pDialog.setMessage("Loading your feed ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }


        @Override
        protected ArrayList<ArrayList<String>> doInBackground(String... params) {
                sqluri = p.getUrifromDB(Import.this);
            sqlcoordinate = p.getCoordinatesfromDB(Import.this);
            sqladdress = p.getAddressfromDB(Import.this);
            sqltime = p.getTimefromDB(Import.this);
            holder.add(sqluri);
            holder.add(sqlcoordinate);
            holder.add(sqladdress);
            holder.add(sqltime);
            return holder;
        }


        @Override
        protected void onPostExecute(ArrayList<ArrayList<String>> arrayLists) {
            ArrayList<String> uris = sqluri;
            ArrayList<String> coordinates = sqlcoordinate;
            ArrayList<String> addresses = sqladdress;
            ArrayList<String> times = sqltime;
            adapter = new PostAdapter(Import.this,R.layout.list_item,uris,coordinates,addresses,times);
            feedList.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            pDialog.dismiss();

        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {   //Intent.ACTION_MEDIA_SCANNER_SCAN_FILE：扫描指定文件


            Intent galleryAddIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            galleryAddIntent.setData(imageUri);

            sendBroadcast(galleryAddIntent);

            Intent sendIntent = new Intent(Import.this,Painting_Activity.class);
            sendIntent.setData(imageUri);
            startActivity(sendIntent);
            Import.this.finish();
        }
        else if(resultCode != RESULT_CANCELED)
        {
            Toast.makeText(this, "There was an error", Toast.LENGTH_SHORT).show();
        }
    }
}
