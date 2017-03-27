package com.app.rakez.poskitchen;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class KitchenHome extends AppCompatActivity {

    private ArrayList<String> orderID = new ArrayList<>();
    private ArrayList<String> qty = new ArrayList<>();
    private ArrayList<String> orderNO = new ArrayList<>();
    private ArrayList<String> tableNO = new ArrayList<>();
    private ArrayList<String> menuID = new ArrayList<>();
    private ArrayList<String> categoryID = new ArrayList<>();
    private ArrayList<String> itemName = new ArrayList<>();
    private ArrayList<String> nonrepeatTable = new ArrayList<>();

    //private ArrayList<TableItem> tableData = new ArrayList<TableItem>();
    private ArrayList<OrderItem> orderData = new ArrayList<OrderItem>();
    private ArrayList<String> indexOfOrder = new ArrayList<>();
    private ArrayList<String> forListOrderNo = new ArrayList<>();
    private ArrayList<String> nonRepeatforListOrderNo = new ArrayList<>();
    private ArrayList<String> forListOrderItem = new ArrayList<>();
    private ArrayList<String> readyTableOrder = new ArrayList<>();
    ArrayAdapter<String> ad;
    ArrayAdapter<OrderItem> itemAdapter;
    String tablNo="";

    ListView tableList;
    ListView itemList;
    private SwipeRefreshLayout swipeRefreshLayout;

    String ipAddress;
    String role="KOT";

    Snackbar sb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen_home);
        SharedPreferences ipPref = getApplicationContext().getSharedPreferences("MyIP", 0);
        ipAddress = ipPref.getString("IPAddress"," ");
        SharedPreferences rolePref = getApplicationContext().getSharedPreferences("MyPrefKitchen", 0);
        String role_id = rolePref.getString("role_id"," ");
        Log.d("dasd","Got here "+role_id);
        if(role_id.equals("2")){
            role="KOT";
        }else{
            role="BOT";
        }
        tableList = (ListView) findViewById(R.id.tableList);
        itemList = (ListView) findViewById(R.id.itemList);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                orderID.clear();
                qty.clear();
                orderNO.clear();
                tableNO.clear();
                menuID.clear();
                categoryID.clear();
                itemName.clear();
                nonrepeatTable.clear();
                makeJsonArrayRequest();
                indexOfOrder.clear();
                forListOrderNo.clear();
                forListOrderItem.clear();
                nonRepeatforListOrderNo.clear();
                orderData.clear();
                itemAdapter.notifyDataSetChanged();
            }
        });
        makeJsonArrayRequest();
        itemAdapter = new CustomAdapter();
        tableList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                tablNo = nonrepeatTable.get(i);
                view.requestFocusFromTouch();
                view.setSelected(true);
                indexOfOrder.clear();
                forListOrderNo.clear();
                forListOrderItem.clear();
                nonRepeatforListOrderNo.clear();
                orderData.clear();
                String tblNo = nonrepeatTable.get(i).substring(10);
                Log.d("jpd","Search here table no"+tblNo);
                for(int j = 0 ; j < tableNO.size() ; j++){
                    if(tableNO.get(j).equals(tblNo)){
                        indexOfOrder.add(String.valueOf(j));
                    }
                }
                Log.d("jpd","Search here size  "+indexOfOrder.size());

                for(int k=0;k<indexOfOrder.size();k++){
                    int position = Integer.parseInt(indexOfOrder.get(k));
                    Log.d("jpd","Search here position "+position);
                    forListOrderNo.add(orderNO.get(position));
                }
                filterRepeatedOrderNo();
                Log.d("jpd","Search here position size repeat"+forListOrderNo.size());
                Log.d("jpd","Search here position size nonrepeat"+nonRepeatforListOrderNo.size());

                for(int k = 0 ; k<nonRepeatforListOrderNo.size() ; k++){
                    String tempOrder = "";
                    Log.d("jpd","Search here position nonRepeat"+nonRepeatforListOrderNo.get(k));
                    for(int j = 0 ; j < indexOfOrder.size() ; j++){
                        int position = Integer.parseInt(indexOfOrder.get(j));
                        if(orderNO.get(position).equals(nonRepeatforListOrderNo.get(k).substring(10))){
                            tempOrder = tempOrder + itemName.get(position)+" "+qty.get(position)+"\n";
                        }
                        Log.d("jpd","Search here orderno non repeat "+orderNO.get(position)+" "+nonRepeatforListOrderNo.get(k).substring(10));
                        Log.d("jpd","Search here temp order "+position+tempOrder);
                    }
                    //forListOrderNo.add("Order No. "+k);
                    forListOrderItem.add(tempOrder);
                }
                for(int count = 0; count <forListOrderItem.size();count++){
                    String sts = "notready";
                    for(int k = 0 ; k < readyTableOrder.size() ; k++){
                        if(readyTableOrder.get(k).equals(nonrepeatTable.get(i)+" "+nonRepeatforListOrderNo.get(count))){
                            sts = "ready";
                            Log.d("ss","Ready data"+nonrepeatTable.get(i)+" "+nonRepeatforListOrderNo.get(count));
                        }
                    }
                    orderData.add(new OrderItem(nonRepeatforListOrderNo.get(count),forListOrderItem.get(count),sts));
                }

                itemList.setAdapter(itemAdapter);

            }
        });

    }

    private void filterRepeatedOrderNo() {
        for(int i=0 ; i<forListOrderNo.size();i++){
            if(!nonRepeatforListOrderNo.contains("Order No. "+forListOrderNo.get(i))){
                nonRepeatforListOrderNo.add("Order No. "+forListOrderNo.get(i));
            }
        }

    }

    private void makeJsonArrayRequest(){
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading");
        pDialog.show();


        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, "http://"+ipAddress+"/orderapp/orderJSON.php?type="+role, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                Log.d("size of the","Size is response "+response.length());
                for(int i = 0;i<response.length();i++){
                    try {
                        JSONObject table = (JSONObject) response.get(i);
                        orderID.add(table.getString("order_id"));
                        qty.add(table.getString("qty"));
                        orderNO.add(table.getString("order_no"));
                        tableNO.add(table.getString("table_no"));
                        menuID.add(table.getString("menu_id"));
                        categoryID.add(table.getString("category_id"));
                        itemName.add(table.getString("name"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                pDialog.hide();
                pDialog.dismiss();
                prepareData();
                swipeRefreshLayout.setRefreshing(false);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.hide();
                pDialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);
                View view = findViewById(R.id.root);
                sb = Snackbar.make(view, "Cannot connect to network", Snackbar.LENGTH_INDEFINITE);
                sb.setAction("Action", null);
                sb.show();

            }
        });
        AppController.getInstance().addToRequestQueue(req);


    }

    private void prepareData() {
        List<Integer> toRemove = new ArrayList<>();
        toRemove.clear();
        for(int i=0 ; i<tableNO.size();i++){
            if(!nonrepeatTable.contains("Table No. "+tableNO.get(i))){
                nonrepeatTable.add("Table No. "+tableNO.get(i));
            }
        }
        for(int i = 0 ; i <readyTableOrder.size() ; i++){
            Log.d("Data New","Data is "+readyTableOrder.get(i));
            boolean isPresent = false;
            String[] parts = readyTableOrder.get(i).split(" ");
            for(int j = 0 ; j <tableNO.size() ; j++){
                Log.d("Data New","Data is1 "+parts[2]);
                Log.d("Data New","Data is2 "+tableNO.get(j));
                if(parts[2].equals(tableNO.get(j))){
                    isPresent = true;
                }
            }
            if(!isPresent){
                toRemove.add(i);
            }
        }
        for (int i = 0; i<toRemove.size();i++){
            Log.d("Data New","To Remove "+toRemove.get(i));
            int delIndex = toRemove.get(i);
            String sts = readyTableOrder.remove(delIndex);
            readyTableOrder.remove(sts);




        }


        ad =new ArrayAdapter<String>(getApplicationContext(),R.layout.table_item,nonrepeatTable);
        tableList.setAdapter(ad);
        ad.notifyDataSetChanged();
        //ad = new CustumTableAdapter();
        //tableList.setAdapter(ad);
        //ad.notifyDataSetChanged();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_kitchenhome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.logout){
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefKitchen", 0); // 0 - for private mode
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();
            Intent in = new Intent(getApplicationContext(),ScrollingActivity.class);
            startActivity(in);
            finish();
        }
        if (id == R.id.setIP) {
            Bundle source = new Bundle();
            Intent in = new Intent(getApplicationContext(),setIP.class);
            source.putString("requestFrom","home");
            in.putExtras(source);
            startActivity(in);
            finish();

        }

        return super.onOptionsItemSelected(item);
    }

    public class CustomAdapter extends ArrayAdapter<OrderItem>{

        public CustomAdapter() {
            super(KitchenHome.this, R.layout.item_view,orderData);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView  = convertView;
            if(convertView==null){
                itemView =getLayoutInflater().inflate(R.layout.item_view,parent,false);
            }
            final OrderItem currentItem = orderData.get(position);
            TextView orderId = (TextView) itemView.findViewById(R.id.orderId);
            TextView orderItem = (TextView) itemView.findViewById(R.id.orderItem);
            final TextView status = (TextView) itemView.findViewById(R.id.staus);
            status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String ordrNo = currentItem.getOrderID();
                    readyTableOrder.add(tablNo+" "+ordrNo);
                    Log.d("status","This is check "+tablNo +" " +ordrNo);
                    status.setBackgroundResource(R.drawable.table_busy);
                }
            });
            String tempStatus = currentItem.getStatus();
            if(tempStatus.equals("ready")){
                status.setBackgroundResource(R.drawable.table_busy);
            }else{
                status.setBackgroundResource(R.drawable.table_free);
            }
            orderId.setText(currentItem.getOrderID());
            orderItem.setText(currentItem.getOrderItem());



            return itemView;
        }
    }

}
