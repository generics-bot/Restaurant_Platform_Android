package com.generics.restaurant;

import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.generics.restaurant.model.Category;
import com.generics.restaurant.model.Dish;
import com.generics.restaurant.model.ServerObject;
import com.generics.restaurant.model.ServerResponse;
import com.generics.restaurant.network.Resources;
import com.generics.restaurant.network.ResponseHandler;

public class MainActivity extends AppCompatActivity {

    private Button buyButton;
    private TextView sumTextView;
    private DrawerLayout main;
    private RecyclerView productsList;
    private RecyclerView.Adapter productsAdapter;
    private RecyclerView.LayoutManager lm;

    private ServerObject[] dishes = new Dish[]{};
    private ServerObject[] categories = new Category[]{};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        buyButton = findViewById(R.id.buyButton);
        sumTextView = findViewById(R.id.sumTextView);
        productsList = findViewById(R.id.productsList);
        lm = new LinearLayoutManager(this);
        setSupportActionBar(toolbar);
        setTitle(R.string.mainMenuTitle);
        main = findViewById(R.id.drawerLayout);
        NavigationView mainNavigation = findViewById(R.id.navigationView);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                main,
                toolbar,
                R.string.openDrawer,
                R.string.closeDrawer);
        main.addDrawerListener(toggle);
        switchToMenu();
        toggle.syncState();
        mainNavigation.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.mainMenuItem :
                    setTitle(R.string.mainMenuTitle);
                    switchToMenu();
                    onBackPressed();
                    break;
                case R.id.cartMenuItem :
                    setTitle(R.string.cartMenuTitle);
                    switchToCart();
                    onBackPressed();
                    break;
                case R.id.optionsMenuItem :
                    //TODO
                    break;
                case R.id.exitItem :
                    finish();
                    break;
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item1 :
                //TODO
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(main.isDrawerOpen(Gravity.START)){
            main.closeDrawer(Gravity.START);
        }else{
            super.onBackPressed();
        }
    }

    private void switchToMenu(){
        buyButton.setVisibility(View.INVISIBLE);
        sumTextView.setVisibility(View.INVISIBLE);
        Resources.getCategories(new ResponseHandler() {
            @Override
            public void onResponse(ServerResponse response) {
                categories = response.getList();
            }

            @Override
            public void onFailure(Throwable t) {
                categories = null;
            }
        });
        productsAdapter = new ProductsAdapter(categories, this);
        ProductsAdapter.cartOpened = false;
        productsList.setLayoutManager(lm);
        productsList.setAdapter(productsAdapter);
        productsList.setHasFixedSize(true);
    }

    public void switchToDishes(int id){
        buyButton.setVisibility(View.INVISIBLE);
        sumTextView.setVisibility(View.INVISIBLE);
        Resources.getDishes(id, new ResponseHandler() {
            @Override
            public void onResponse(ServerResponse response) {
                dishes = response.getList();
            }

            @Override
            public void onFailure(Throwable t) {
                dishes = null;
            }
        });
        productsAdapter = new ProductsAdapter(dishes, this);
        ProductsAdapter.cartOpened = false;
        productsList.setLayoutManager(lm);
        productsList.setAdapter(productsAdapter);
        productsList.setHasFixedSize(true);
    }

    private void switchToCart(){
        buyButton.setVisibility(View.VISIBLE);
        sumTextView.setVisibility(View.VISIBLE);
        int sum = 0;
        for(Dish temp : Resources.getUserCart()){
            sum += temp.getPrice();
        }
        sumTextView.setText(String.valueOf(sum));
        productsAdapter = new ProductsAdapter(Resources.getUserCartAsServerObject(), this);
        ProductsAdapter.cartOpened = true;
        productsList.setLayoutManager(lm);
        productsList.setAdapter(productsAdapter);
        productsList.setHasFixedSize(true);
        buyButton.setOnClickListener(event -> {
            Resources.addOrder(Resources.getCurrentUser().getToken(), Resources.toJSONArray(Resources.getUserCartAsDishes()), Resources.getCurrentUser().getId(), 0,
                    new ResponseHandler(){
                        @Override
                        public void onResponse(ServerResponse response) {
                            Toast.makeText(getBaseContext(), "Successfully ordered!", Toast.LENGTH_SHORT).show();
                            //TODO MAYBE
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            Log.i("BLYAT EXCEPTION : \n", t.getMessage());
                        }
                    });
        });
    }
}
