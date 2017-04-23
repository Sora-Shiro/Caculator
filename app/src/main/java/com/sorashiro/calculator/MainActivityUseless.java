package com.sorashiro.calculator;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.sorashiro.calculator.exception.CustomerException;
import com.sorashiro.calculator.util.AnimationUtil;
import com.sorashiro.calculator.util.AppSaveDataSPUtil;

import java.math.BigDecimal;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Sora
 * @date 2017.4.23
 * <p>
 * Using BigDecimal to calculate, but can't handle infinite cycle number.
 * 使用BigDecimal计算，但是不能处理无限循环数
 */

@Deprecated
public class MainActivityUseless extends AppCompatActivity {

    private final BigDecimal MAX = new BigDecimal(0x7ff0000000000000L);
    private final BigDecimal MIN = new BigDecimal(0xfff0000000000000L);

    @BindView(R.id.text_calc)
    TextView mTextCalc;
    @BindView(R.id.text_result)
    EditText mTextResult;

    StringBuilder mStringBuilder;

    //自动计算
    boolean mIfAutoCalc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mStringBuilder = new StringBuilder("0");
        mTextCalc.setText("0");
        mTextCalc.setMovementMethod(ScrollingMovementMethod.getInstance());

        AppSaveDataSPUtil.init(this);
        mIfAutoCalc = AppSaveDataSPUtil.getIfAutoCalc();

    }

    private boolean isOperator(char c) {
        return (c == '+' || c == '-' || c == '*' || c == '/');
    }

    @OnClick({R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
            R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
            R.id.btn_dot, R.id.btn_multi, R.id.btn_divide, R.id.btn_sub, R.id.btn_plus,
            R.id.btn_left_p, R.id.btn_right_p, R.id.btn_c, R.id.btn_del, R.id.btn_equal})
    public void onBtnClick(View view) {
        Button button = (Button) view;
        if (view.getId() != R.id.btn_equal)
            mTextResult.setText("Result Area");
        if (mStringBuilder.charAt(mStringBuilder.length() - 1) == '=') {
            mStringBuilder.deleteCharAt(mStringBuilder.length() - 1);
        }
        switch (view.getId()) {
            case R.id.btn_0:
            case R.id.btn_1:
            case R.id.btn_2:
            case R.id.btn_3:
            case R.id.btn_4:
            case R.id.btn_5:
            case R.id.btn_6:
            case R.id.btn_7:
            case R.id.btn_8:
            case R.id.btn_9:
            case R.id.btn_left_p:
            case R.id.btn_right_p:
                if (mStringBuilder.length() == 1 && mStringBuilder.charAt(0) == '0') {
                    mStringBuilder.deleteCharAt(0);
                }
                mStringBuilder.append(button.getText());
                break;
            case R.id.btn_plus:
            case R.id.btn_sub:
            case R.id.btn_multi:
            case R.id.btn_divide:
                if (isOperator(mStringBuilder.charAt(mStringBuilder.length() - 1))) {
                    mStringBuilder.deleteCharAt(mStringBuilder.length() - 1);
                }
                mStringBuilder.append(button.getText());
                break;
            case R.id.btn_dot:
                mStringBuilder.append(button.getText());
                break;
            case R.id.btn_del:
                if (mStringBuilder.length() > 0) {
                    mStringBuilder.deleteCharAt(mStringBuilder.length() - 1);
                    if (mStringBuilder.length() == 0) {
                        mStringBuilder.append('0');
                    }
                }
                break;
            case R.id.btn_c:
                mStringBuilder = new StringBuilder("0");
                break;
            case R.id.btn_equal:
                mStringBuilder.append('=');
                changeCalcText();
                toCalcResult();
                break;
        }
        changeCalcText();
        if (mIfAutoCalc) {
            toCalcResult();
        }
    }

    private void changeCalcText() {
        String text = mStringBuilder.toString();
        mTextCalc.setText(text);
    }

    private void toCalcResult() {
        String toCalc = mTextCalc.getText().toString();
        String resultString = "";
        try {
            resultString = calcResult(toCalc, new Stack<Character>(), new Stack<BigDecimal>());
        } catch (CustomerException e) {
            e.printStackTrace();
            resultString = e.getMsgDes();
        }
        mTextResult.setText(resultString);
    }

    private String calcResult(String what, Stack<Character> operation, Stack<BigDecimal> num) throws CustomerException {
        char[] ss = what.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();

        int i;
        for (i = 0; i < ss.length; i++) {
            char ch = ss[i];
            if (ch <= '9' && ch >= '0') {
                stringBuilder.append(ch);
            } else if (ch == '.') {
                if (stringBuilder.indexOf(".") == -1) {
                    if (stringBuilder.length() == 0) {
                        stringBuilder.append('0');
                    }
                    stringBuilder.append('.');
                } else {
                    throw new CustomerException("Multi '.' in a number.");
                }
            }
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {
                //检查运算符前的数字
                if (ss[i - 1] == '.') {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    num.push(new BigDecimal(Double.parseDouble(stringBuilder.toString())));
                    stringBuilder = new StringBuilder();
                } else if (ss[i - 1] <= '9' && ss[i - 1] >= '0') {
                    num.push(new BigDecimal(Double.parseDouble(stringBuilder.toString())));
                    stringBuilder = new StringBuilder();
                } else if (ss[i - 1] == '(') {
                    if (ch == '*' || ch == '/') {
                        throw new CustomerException("There can't be '*' and '/' after '('.");
                    } else {
                        num.push(new BigDecimal(0.0));
                    }
                }
                //乘除法检测
                checkMulAndDiv(operation, num);
                operation.push(ch);
            }
            if (ch == '(') {
                operation.push(ch);
            }
            if (ch == ')') {
                if (i == 0) {
                    throw new CustomerException("')' can't be first.");
                }
                //检查括号前的数字
                if (ss[i - 1] == '.') {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    num.push(new BigDecimal(Double.parseDouble(stringBuilder.toString())));
                    stringBuilder = new StringBuilder();
                } else if (ss[i - 1] <= '9' && ss[i - 1] >= '0') {
                    num.push(new BigDecimal(Double.parseDouble(stringBuilder.toString())));
                    stringBuilder = new StringBuilder();
                } else if (stringBuilder.length() != 0){
                    //                    num.pop();
                } else {
                    throw new CustomerException("There should be a num before ')'.");
                }
                //乘除法检测
                checkMulAndDiv(operation, num);
                //直到遇到左括号为止，不断逆向读取运算符和数字
                Stack<Character> subO = new Stack<>();
                Stack<BigDecimal> subNumS = new Stack<>();
                if (num.size() == 0) {
                    throw new CustomerException("Need some num.");
                }
                while (operation.size() > 0) {
                    if (subO.size() > 0 && subO.peek() == '(')
                        break;
                    subO.push(operation.pop());
                    subNumS.push(num.pop());
                }
                if (subO.size() == 0 || subO.peek() != '(') {
                    throw new CustomerException("Need more '('.");
                }
                //弹出左括号
                subO.pop();
                //计算括号内值
                while (!subO.empty()) {
                    char o = subO.pop();
                    if (subNumS.size() == 1) {
                        break;
                    }
                    BigDecimal num1 = subNumS.pop();
                    BigDecimal num2 = subNumS.pop();
                    BigDecimal result = new BigDecimal(0.0);
                    if (o == '+') {
                        if (num1.add(num2).compareTo(MAX) == 1 || num1.add(num2).compareTo(MIN) == -1) {
                            throw new CustomerException("Too big/small value");
                        }
                        result = num1.add(num2);
                    } else if (o == '-') {
                        if (num1.subtract(num2).compareTo(MAX) == 1 || num1.subtract(num2).compareTo(MIN) == -1) {
                            throw new CustomerException("Too big/small value");
                        }
                        result = num1.subtract(num2);
                    }
                    subNumS.push(result);
                }
                BigDecimal subResult = subNumS.pop();
                num.push(subResult);
                stringBuilder = new StringBuilder(subResult+"");
            }
        }
        if (stringBuilder.length() != 0) {
            //检查最后的数字
            char last = stringBuilder.charAt(stringBuilder.length() - 1);
            if (last == '.') {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                num.push(new BigDecimal(Double.parseDouble(stringBuilder.toString())));
            } else if (last <= '9' && last >= '0') {
                num.push(new BigDecimal(Double.parseDouble(stringBuilder.toString())));
            }
            //乘除法检测
            checkMulAndDiv(operation, num);
        } else {
            //最后如果不是数字且不是')'的处理
            if (!operation.empty() && ss[i-1] != ')') {
                throw new CustomerException("Last character error.");
            }
        }
        //进行最后计算，如果遇到左括号抛出异常
        Stack<Character> reverseO = new Stack<>();
        while (!operation.empty()) {
            if (operation.peek() == '(') {
                throw new CustomerException("Need more ')'");
            }
            reverseO.push(operation.pop());
        }
        Stack<BigDecimal> reverseNum = new Stack<>();
        while (!num.empty()) {
            reverseNum.push(num.pop());
        }
        while (!reverseO.empty()) {
            char o = reverseO.pop();
            if (reverseNum.size() == 1) {
                return reverseNum.pop().toString();
            }
            BigDecimal num1 = reverseNum.pop();
            BigDecimal num2 = reverseNum.pop();
            BigDecimal result = new BigDecimal(0.0);
            if (o == '+') {
                if (num1.add(num2).compareTo(MAX) == 1 || num1.add(num2).compareTo(MIN) == -1) {
                    throw new CustomerException("Too big/small value");
                }
                result = num1.add(num2);
            } else if (o == '-') {
                if (num1.subtract(num2).compareTo(MAX) == 1 || num1.subtract(num2).compareTo(MIN) == -1) {
                    throw new CustomerException("Too big/small value");
                }
                result = num1.subtract(num2);
            }
            reverseNum.push(result);
        }


        return reverseNum.pop().toString();
    }

    //乘除法检测
    private void checkMulAndDiv(Stack<Character> operation, Stack<BigDecimal> num) {
        if (operation.size() > 0) {
            char o = operation.peek();
            if (o == '*' || o == '/') {
                BigDecimal num1 = num.pop();
                BigDecimal num2 = num.pop();
                if (o == '*') {
                    if (num1.multiply(num2).compareTo(MAX) == 1 || num1.multiply(num2).compareTo(MIN) == -1) {
                        throw new CustomerException("Too big/small value.");
                    } else {
                        num.push(num1.multiply(num2));
                    }
                }
                if (o == '/') {
                    try {
                        num.push(num2.divide(num1));
                    } catch (Exception e) {
                        throw new CustomerException(e.getMessage());
                    }
                }
                operation.pop();
            }
        }
    }

    @OnClick(R.id.btn_config)
    public void onConfigClick(View view) {
        AnimationUtil.twinkle(view);
        configDialog();
    }

    private void configDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityUseless.this);
        View view = LayoutInflater.from(MainActivityUseless.this).inflate(R.layout.dialog_config, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.check_auto_calc);
        checkBox.setChecked(AppSaveDataSPUtil.getIfAutoCalc());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isCheck) {

            }
        });
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                boolean isCheck = checkBox.isChecked();
                mIfAutoCalc = isCheck;
                AppSaveDataSPUtil.setIfAutoCalc(isCheck);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //什么都不做
            }
        });
        builder.show();
    }

}
