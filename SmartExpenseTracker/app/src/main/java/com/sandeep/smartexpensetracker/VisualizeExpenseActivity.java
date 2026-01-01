package com.sandeep.smartexpensetracker;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VisualizeExpenseActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = "com.sandeep.smartexpensetracker.VisualizeExpenseActivity";
    PieChart pieChart;
    Button btnExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_visualize_expense);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        pieChart = findViewById(R.id.piechart);
        btnExport = findViewById(R.id.btnExport);
        btnExport.setOnClickListener(this);

        setData();
    }

    private void setData()
    {
        // Set the percentage of language used
       /* tvFOOD.setText(Integer.toString(40));
        tvGROCERIES.setText(Integer.toString(30));
        tvTRAVEL.setText(Integer.toString(5));
        tvOTHER.setText(Integer.toString(25));*/
        HashMap<String, Double> map = new HashMap<>();
        map.put("TRAVEL", (Double)0.0);
        map.put("GROCERIES", (Double)0.0);
        map.put("OTHER", (Double)0.0);
        map.put("FOOD", (Double)0.0);
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        double[] total = {0};

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // Get the data from the database on a background thread
                List<CategoryTotal> categoryTotals = DatabaseManager.getDatabaseInstance(getApplicationContext()).getDB().expenseDAO().getSumGroupedByCategory();
                for(CategoryTotal cat: categoryTotals){
                    map.put(cat.getCategory(), (Double) cat.getTotal_amount());
                    total[0] +=(Double) cat.getTotal_amount();
                    Log.d("Sandeep", cat.getCategory()+"**"+cat.getTotal_amount());
                }


                HashMap<String, Double> mapp = Util.calculatePercentages(total, map);
                Log.d("Sandeep", "size"+ mapp.size());
                // PieModel Elements to be add created
                PieModel p1 = new PieModel("FOOD",Integer.parseInt(String.valueOf((int)Math.round(mapp.get("FOOD")))),
                        Color.parseColor("#FFA726"));

                PieModel p2 = new PieModel("GROCERIES",Integer.parseInt(String.valueOf((int)Math.round(mapp.get("GROCERIES")))),
                        Color.parseColor("#66BB6A")) ;

                PieModel p3 = new PieModel("TRAVEL",Integer.parseInt(String.valueOf((int)Math.round(mapp.get("TRAVEL")))),
                        Color.parseColor("#EF5350"));

                PieModel p4 = new PieModel("OTHER",Integer.parseInt(String.valueOf((int)Math.round(mapp.get("OTHER")))),
                        Color.parseColor("#29B6F6"));

                // Update the UI on the main thread after fetching data
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Set the data and color to the pie chart
                        pieChart.addPieSlice(p1);
                        pieChart.addPieSlice(p2);
                        pieChart.addPieSlice(p3);
                        pieChart.addPieSlice(p4);
                        // To animate the pie chart
                        pieChart.startAnimation();
                    }
                });
            }
        });



    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");
        switch(v.getId()) {
            case R.id.btnExport: {
                new Thread(this::exportDataToCSV).start();
            }
            break;
        }
    }

    private void exportDataToCSV() {
        List<Expense> expenses = DatabaseManager.getDatabaseInstance(this).getDB().expenseDAO().getTotalExpense();

        // File path (external storage)
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        String fileName = String.valueOf(System.currentTimeMillis())+".csv";
        File file = new File(path, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            // Write the CSV header
            writer.append("ExpenseID,Title,Amount,Category,Note,Date\n");

            // Write data
            for (Expense expense : expenses) {
                writer.append(expense.getExpenseid() + ",")
                        .append(expense.getTitle() + ",")
                        .append(expense.getAmount() + ",")
                        .append(expense.getCategory() + ",")
                        .append(expense.getNote() + ",")
                        .append(expense.getDate() + "\n");
            }
            // Show a success message
            runOnUiThread(() -> Toast.makeText(this, "Exported to CSV successfully", Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Failed to export", Toast.LENGTH_SHORT).show());
        }
    }
}