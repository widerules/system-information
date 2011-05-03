package sys.info.jtbuaa;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalPayment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class About extends Activity implements OnClickListener{
	

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		CheckoutButton launchPayPalButton = PayPal.getInstance().getCheckoutButton(this, PayPal.BUTTON_152x33, CheckoutButton.TEXT_PAY);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,	LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.bottomMargin = 10;
		launchPayPalButton.setLayoutParams(params);
		launchPayPalButton.setOnClickListener(this);
		((LinearLayout)findViewById(R.id.about)).addView(launchPayPalButton);
		
		TextView tv = (TextView)findViewById(R.id.myversion);
		tv.setText(getIntent().getStringExtra("version"));
	}

	public void onClick(View arg0) {
		PayPalPayment newPayment = new PayPalPayment();
		EditText et = (EditText)findViewById(R.id.themoney);
		BigDecimal bd = new BigDecimal(Integer.parseInt(et.getText().toString())); 
		newPayment.setSubtotal(bd);
		newPayment.setPaymentSubtype(1);
		newPayment.setCurrencyType("USD");
		newPayment.setRecipient("jtbuaa@gmail.com");
		
		Intent paypalIntent = PayPal.getInstance().checkout(newPayment, this);
		this.startActivityForResult(paypalIntent, 1);
	}
}
