package com.xcx.accessibilitymonitor.view.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.xcx.accessibilitymonitor.MyApp
import com.xcx.accessibilitymonitor.R
import com.xcx.accessibilitymonitor.utils.setScreenPassWord

/**
 * Create By Ruge at 2019-04-01
 */
class CustomDialog : DialogFragment(), View.OnClickListener {

    internal lateinit var btClose: TextView
    private lateinit var tv_passwd_1: TextView
    private lateinit var tv_passwd_2: TextView
    private lateinit var tv_passwd_3: TextView
    private lateinit var tv_passwd_4: TextView
    private lateinit var tv_passwd_5: TextView
    private lateinit var tv_passwd_6: TextView
    private lateinit var et_support_num: EditText
    lateinit var text_list: ArrayList<TextView>

    var preLength = 0
    var curLength = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_input_passwd, container, false)
        tv_passwd_1 = view.findViewById(R.id.tv_passwd_1)
        tv_passwd_2 = view.findViewById(R.id.tv_passwd_2)
        tv_passwd_3 = view.findViewById(R.id.tv_passwd_3)
        tv_passwd_4 = view.findViewById(R.id.tv_passwd_4)
        tv_passwd_5 = view.findViewById(R.id.tv_passwd_5)
        tv_passwd_6 = view.findViewById(R.id.tv_passwd_6)
        et_support_num = view.findViewById(R.id.et_support_num)
        text_list = arrayListOf(tv_passwd_1, tv_passwd_2, tv_passwd_3, tv_passwd_4, tv_passwd_5, tv_passwd_6)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener(view)

    }

    private fun initListener(view: View) {
        btClose = view.findViewById(R.id.tv_dialog_cancel)
        tv_passwd_1.isSelected = true
        tv_passwd_1.setOnClickListener(this)
        tv_passwd_2.setOnClickListener(this)
        tv_passwd_3.setOnClickListener(this)
        tv_passwd_4.setOnClickListener(this)
        tv_passwd_5.setOnClickListener(this)
        tv_passwd_6.setOnClickListener(this)

        btClose.setOnClickListener {
            dismiss()
        }

        et_support_num.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                Log.d("xcx", "afterTextChanged: ${s?.length} ${s.toString()}")

                if (s == null) {
                    return
                }

                if (s.isEmpty()) {
                    curLength = 0
                    text_list[0].text = ""
                    text_list[0].isSelected = true
                    text_list[1].isSelected = false
                    return
                }

                if (curLength > s.length) {//删除操作
                    text_list[s.length].text = ""
                    clearTextSelect(s.length + 1)

                } else {//输入增加
                    text_list[s.length - 1].text = s.toString().toCharArray()[curLength].toString()
                    if (s.length < 6) {
                        text_list[s.length].isSelected = true

                    }
                }
                curLength = s.length
                if (s.length == 6) {
                    savePassWd()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    private fun savePassWd() {
        if (activity == null) {
            Toast.makeText(MyApp.appContext, "保存失败呢,稍后试一下吧", Toast.LENGTH_SHORT).show()
            dismiss()
            return
        }
        var pd = et_support_num.text.toString()
        setScreenPassWord(pd)
        Toast.makeText(MyApp.appContext, "$pd 保存成功!", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun clearTextSelect(length: Int) {
        for (i in length..5) {
            text_list[i].isSelected = false
        }
    }

    override fun onStart() {
        super.onStart()
        val win = dialog.window
        // 一定要设置Background，如果不设置，window属性设置无效
        win.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val param = win.attributes
        param.width = ViewGroup.LayoutParams.MATCH_PARENT
//        param.gravity = dialogGravity
        win.attributes = param
        showInput()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_passwd_1, R.id.tv_passwd_2, R.id.tv_passwd_3, R.id.tv_passwd_4, R.id.tv_passwd_5, R.id.tv_passwd_6 -> {
                showInput()
            }
        }
    }

    /**
     * 弹出键盘
     */
    private fun showInput() {
        et_support_num.isFocusable = true
        et_support_num.isFocusableInTouchMode = true
        et_support_num.requestFocus()
        activity?.apply {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
        }


    }

}