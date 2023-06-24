package com.example.distantdb;

import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    Button btn,btnDelete,btnUpdate;
    EditText edit;
    ListView listTasks;
    ArrayAdapter<String> adapter = null;
    // String url = "http://10.0.2.2:80/calendar/"
    String url = "http://10.0.2.2:80/calendar/";
    List<Task> taskList = new ArrayList<>();
    int selectedTaskIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listTasks = findViewById(R.id.listTasks);
        btn = findViewById(R.id.addButton);
        btnDelete = findViewById(R.id.deleteButton);
        btnUpdate = findViewById(R.id.updateButton);
        edit = findViewById(R.id.editTask);


       //Création  d'instance de la classe Retrofit pour envoyer des requêtes HTTP et gérer les réponses.
        Retrofit retrofit = new Retrofit.Builder( ).baseUrl(url).addConverterFactory(GsonConverterFactory.create( )).build( );

        // Création d'nstance de l'interface myapi à l'aide de Retrofit pour appeler les services Web de l'API.
        myapi api = retrofit.create(myapi.class);

       // L'objet Call<List<Task>> est utilisé pour exécuter la requête et recevoir la réponse, qui sera une liste d'objets Task.
        Call<List<Task>> call = api.getalltasks( );

        // L'exécution de la requête de manière asynchrone en utilisant enqueue(), la requête sera effectuée en arrière-plan sans bloquer le thread principal.
        call.enqueue(new Callback<List<Task>>( ) {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                List<Task> data = response.body( );
                if (data != null) {
                    taskList.clear();
                    taskList.addAll(data);
                    adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, Listin(taskList));
                    listTasks.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                System.out.println(t);
            }
        });
        listTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTaskIndex = i ;
                edit.setText(taskList.get(i).getTask());
            }
        });

        btn.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View v) {

                process( );
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedTaskIndex != -1) {
                    Task selectedTask = taskList.get(selectedTaskIndex);
                    deleteTask(selectedTask);
                    edit.setText("");
                    selectedTaskIndex = -1;
                }
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedTaskIndex != -1) {
                    Task selectedTask = taskList.get(selectedTaskIndex);
                    updateTask(selectedTask);
                    edit.setText("");
                }
            }
        });
    }

    public void process() {
        Retrofit retrofit = new Retrofit.Builder( )
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create( ))
                .build( );

        myapi api = retrofit.create(myapi.class);
        Call<Task> call = api.adtask(edit.getText( ).toString( ));
        call.enqueue(new Callback<Task>( ) {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {

                edit.setText("");

                Log.i("reponse retrofit", response.toString( ));
                Toast.makeText(getApplicationContext(), "Inseré avec success", Toast.LENGTH_LONG).show();

                updateTaskList();
            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                System.out.println(t);
            }
        });


    }


    ArrayList Listin(List<Task> l) {
        ArrayList<String> maliste = new ArrayList<>( );
        for (int i = 0; i < l.size( ); i++) {
            maliste.add(l.get(i).getId( ) + ": " + l.get(i).getTask( ));
        }
        return maliste;
    }
    private void updateTaskList(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        myapi api = retrofit.create(myapi.class);
        Call<List<Task>> call = api.getalltasks();

        call.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                List<Task> data = response.body();
                if (data != null) {
                    taskList.clear();
                    taskList.addAll(data);
                    adapter.notifyDataSetChanged();
                    adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, Listin(taskList));
                    listTasks.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                System.out.println(t);
            }
        });
    }


    private void deleteTask(Task task) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        myapi api = retrofit.create(myapi.class);
        Call<Task> call = api.deleteTask(task.getId());
        call.enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {
                    updateTaskList();
                    Toast.makeText(getApplicationContext(), "Supprimé avec success", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                System.out.println(t);
            }
        });
    }


    private void updateTask(Task task) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        myapi api = retrofit.create(myapi.class);
        Call<Task> call = api.updateTask(task.getId(),edit.getText( ).toString());
        call.enqueue(new Callback<Task>() {
            @Override
            public void onResponse(Call<Task> call, Response<Task> response) {

                    updateTaskList();
                    Toast.makeText(getApplicationContext(), "Modifié avec success", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onFailure(Call<Task> call, Throwable t) {
                System.out.println(t);
            }
        });
    }
}





