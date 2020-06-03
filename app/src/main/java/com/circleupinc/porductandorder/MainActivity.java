package com.circleupinc.porductandorder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.circleupinc.productandorder.Order.Fulfillment;
import com.circleupinc.productandorder.Order.PaymentStatus;
import com.circleupinc.productandorder.Product.ChangePrice;
import com.circleupinc.productandorder.Product.ProductDetails;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        startActivity(new Intent(MainActivity.this, Fulfillment.class));



    }
}
