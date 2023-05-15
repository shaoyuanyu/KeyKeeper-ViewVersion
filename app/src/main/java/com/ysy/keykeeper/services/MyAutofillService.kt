package com.ysy.keykeeper.services

import android.R.id
import android.annotation.SuppressLint
import android.app.assist.AssistStructure
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import android.util.Log
import android.view.View.IMPORTANT_FOR_AUTOFILL_YES
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.Button
import android.widget.EditText
import android.widget.RemoteViews
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.ysy.keykeeper.basic_helper.MyDatabaseHelper


@RequiresApi(Build.VERSION_CODES.O)
class MyAutofillService : AutofillService() {

    // 账号/密码识别关键词
    val accountKeyword = arrayListOf<String>("username", "Username", "account", "Account", "用户名", "账户", "账号", "号", "用戶名", "賬戶", "賬號", "號")
    val passwdKeyword = arrayListOf<String>("password", "Password", "密码", "密碼")

    // 解析Structure后得到的AutofillId
    data class ParsedStructure(var usernameId: AutofillId?, var passwordId: AutofillId?)
    var parsedStructure = ParsedStructure(null, null)

    // 账号密码
    data class UserData(var username: String?, var password: String?)
    var userDataToSave = UserData(null, null)

    /**
     * 自动填充账号密码
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onFillRequest(request: FillRequest, cancellationSignal: CancellationSignal, callback: FillCallback) {

        Log.i("自动填充", "填充请求")

        // 初始化
        parsedStructure.usernameId = null
        parsedStructure.passwordId = null

        // Get the structure from the request
        val context: List<FillContext> = request.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure

        // Traverse the structure looking for nodes to fill out.
        parseStructure(structure)

        if( this.parsedStructure.usernameId == null || this.parsedStructure.passwordId == null ) {
            Log.i("自动填充", "未检测到")
            return;
        } else {
            Log.i("自动填充", "检测到 ${this.parsedStructure.usernameId} ${this.parsedStructure.passwordId}")
        }

        // 从数据库读取账号信息
        val accountList = fetchUserData(request.fillContexts[0].structure.activityComponent.packageName)
        if (accountList.isEmpty()) {
            callback.onSuccess(null)
            return;
        }

        // fillResponse
        val fillResponseBuilder = FillResponse.Builder()

        for (accountInfo in accountList) {
            Log.d("自动填充", "账号 :: ${accountInfo.username}")

            val usernamePresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            usernamePresentation.setTextViewText(android.R.id.text1, accountInfo.username + "账号")

            val passwordPresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            passwordPresentation.setTextViewText(android.R.id.text1, "***")

            val dataset = Dataset.Builder()
                .setField( this.parsedStructure.usernameId as AutofillId, setFieldBuilder(usernamePresentation, accountInfo.username!!).build() )
                .setField( this.parsedStructure.passwordId as AutofillId, setFieldBuilder(null, accountInfo.password!!).build() )
                .build()

            fillResponseBuilder.addDataset(dataset)
        }
        fillResponseBuilder.setSaveInfo(
            SaveInfo.Builder(
                SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                arrayOf(parsedStructure.usernameId, parsedStructure.passwordId)
            ).build()
        )
        val fillResponse: FillResponse = fillResponseBuilder.build()

        callback.onSuccess(fillResponse)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun setFieldBuilder(presentation: RemoteViews?, text: String) : Field.Builder {

        // field
        val fieldBuilder = Field.Builder()

        // value
        //if (value != null) {
        //    fieldBuilder.setValue(value)
        //}
        fieldBuilder.setValue( AutofillValue.forText( text ) )

        // filter
        //if (filter != null) {
        //    fieldBuilder.setFilter(filter)
        //}

        // presentation
        if ( presentation != null ) {
            val presentationsBuilder = Presentations.Builder()
            presentationsBuilder.setMenuPresentation(presentation)

            // inlinePresentation
            //if (inlinePresentation != null) {
            //    presentationsBuilder.setInlinePresentation(inlinePresentation)
            //}

            // dialogPresentation
            //if (dialogPresentation != null) {
            //    presentationsBuilder.setDialogPresentation(dialogPresentation)
            //}
            presentationsBuilder.setDialogPresentation(presentation)

            fieldBuilder.setPresentations( presentationsBuilder.build() )
        }

        return fieldBuilder
    }

    /**
     * 获取账号密码
     */
    @SuppressLint("Range")
    fun fetchUserData( thePackageName : String ): ArrayList<UserData> {
        var accountList = arrayListOf<UserData>()

        var cursor = MyDatabaseHelper(this).readDbByUrl(thePackageName)
        Log.i("自动填充 读取账号信息", "包名 :: ${thePackageName}")

        if ( cursor.moveToFirst() ) {
            do {
                accountList.add( UserData( cursor.getString(cursor.getColumnIndex("account")), cursor.getString(cursor.getColumnIndex("passwd")) ) )
            } while (cursor.moveToNext())
        }

        return accountList
    }

    /**
     * 解析Structure
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun parseStructure(structure: AssistStructure) {
        Log.i("自动填充", "检测中...")
        var usernameId: AutofillId? = null
        var passwordId: AutofillId? = null

        val windowNodes: List<AssistStructure.WindowNode> = structure.run { (0 until windowNodeCount).map { getWindowNodeAt(it) } }

        windowNodes.forEach { windowNode: AssistStructure.WindowNode ->
            val viewNode: AssistStructure.ViewNode? = windowNode.rootViewNode
            parseNode(viewNode)
        }
    }

    /**
     * 解析Node
     * 递归
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun parseNode(viewNode: AssistStructure.ViewNode?) {
        if ( spotKeyword(viewNode, accountKeyword) ) {
            Log.d("自动填充", " ${viewNode?.id.toString()} : ${viewNode?.contentDescription.toString()} :: ${viewNode?.hint.toString()},  ${viewNode?.text.toString()}")
            if ( viewNode?.autofillId != null ) {
                this.parsedStructure.usernameId = viewNode?.autofillId
            }
        } else if ( spotKeyword(viewNode, passwdKeyword) ) {
            if ( viewNode?.autofillId != null ) {
                this.parsedStructure.passwordId = viewNode?.autofillId
            }
        }

        // 递归Child
        val children: List<AssistStructure.ViewNode>? = viewNode?.run { (0 until childCount).map { getChildAt(it) } }
        children?.forEach { childNode: AssistStructure.ViewNode ->
            parseNode(childNode)
        }
    }

    /**
     * 关键词检测
     * 用于判断账号/密码输入框
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun spotKeyword(viewNode: AssistStructure.ViewNode?, keywordList : ArrayList<String> ) : Boolean {

        // 黑名单
        //
        // ----

        //Log.d("自动填充 关键词识别", "${viewNode?.autofillHints}, ${viewNode?.importantForAutofill}")

        // 筛选
        for (word in keywordList) {
            if ( viewNode?.autofillHints?.contains(word) == true ) {
                // 优先通过autofillHints判断
                return true
            } else if ( viewNode?.className.equals(EditText::class.java.name) || viewNode?.isFocusable == true ) {
                // || viewNode?.isFocusable == true

                // 黑名单
                if ( viewNode?.className.equals(Button::class.java.name) ) {
                    return false
                }
                if ( viewNode?.hint.toString().contains("忘记密码") || viewNode?.text.toString().contains("忘记密码") ) {
                    return false
                }

                if (viewNode?.contentDescription.toString().contains(word)) {
                    return true
                } else if (viewNode?.hint.toString().contains(word)) {
                    return true
                } else if (viewNode?.text.toString().contains(word)) {
                    return true
                }
            }
        }

        return false
    }



    /**
     * 保存新账号密码
     */
    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {

        this.userDataToSave.username = null
        this.userDataToSave.password = null

        // Get the structure from the request
        val context: List<FillContext> = request.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure

        // Traverse the structure looking for data to save
        traverseStructure(structure)

        // Persist the data, if there are no errors, call onSuccess()
        callback.onSuccess()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun traverseStructure(structure: AssistStructure) {
        val windowNodes: List<AssistStructure.WindowNode> = structure.run { (0 until windowNodeCount).map { getWindowNodeAt(it) } }

        windowNodes.forEach { windowNode: AssistStructure.WindowNode ->
            val viewNode: AssistStructure.ViewNode? = windowNode.rootViewNode
            traverseNode(viewNode)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun traverseNode(viewNode: AssistStructure.ViewNode?) {
        if (viewNode?.autofillHints?.isNotEmpty() == true) {
            if ( spotKeyword(viewNode, accountKeyword) ) {
                this.userDataToSave.username = viewNode?.text.toString()
            } else if ( spotKeyword(viewNode, passwdKeyword) ) {
                this.userDataToSave.password = viewNode?.text.toString()
            }
        } else {
            // Or use your own heuristics to describe the contents of a view
            // using methods such as getText() or getHint().
        }

        val children: List<AssistStructure.ViewNode>? = viewNode?.run { (0 until childCount).map { getChildAt(it) } }

        children?.forEach { childNode: AssistStructure.ViewNode ->
            traverseNode(childNode)
        }
    }

    fun saveUserData() {
        ;
    }

}