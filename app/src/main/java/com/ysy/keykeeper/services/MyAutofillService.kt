package com.ysy.keykeeper.services

import android.annotation.SuppressLint
import android.app.assist.AssistStructure
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.EditText
import android.widget.RemoteViews
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.ysy.keykeeper.basic_helper.MyDatabaseHelper

@RequiresApi(Build.VERSION_CODES.O)
class MyAutofillService : AutofillService() {

    // 账号/密码识别关键词
    val accountKeyword = arrayListOf<String>("username", "Username", "account", "Account", "用户名", "账户", "账号", "号")
    val passwdKeyword = arrayListOf<String>("password", "Password", "密码")

    // 解析Structure后得到的AutofillId
    data class ParsedStructure(var usernameId: AutofillId?, var passwordId: AutofillId?)
    var parsedStructure = ParsedStructure(null, null)

    // 账号密码
    data class UserData(var username: String, var password: String)

    /**
     * 自动填充账号密码
     */
    override fun onFillRequest(request: FillRequest, cancellationSignal: CancellationSignal, callback: FillCallback) {
        Log.i("自动填充", "填充请求")
        parsedStructure.usernameId = null
        parsedStructure.passwordId = null

        // Get the structure from the request
        //val context: List<FillContext> = request.fillContexts
        //val structure: AssistStructure = context[context.size - 1].structure
        var fillContexts: List<FillContext> = request.getFillContexts()
        val structure: AssistStructure = fillContexts.get(fillContexts.size - 1).getStructure()

        // Traverse the structure looking for nodes to fill out.
        //val parsedStructure: ParsedStructure = parseStructure(structure)
        parseStructure(structure)

        if( this.parsedStructure.usernameId == null || this.parsedStructure.passwordId == null ) {
            Log.i("自动填充", "未检测到")
            return;
        }

        // 从数据库读取账号信息
        val accountList = fetchUserData(request)
        if (accountList.isEmpty()) {
            return;
        }

        // Add a dataset to the response
        val fillResponseBuilder = FillResponse.Builder()
            .setSaveInfo(
                SaveInfo.Builder(
                    SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                    arrayOf(parsedStructure.usernameId, parsedStructure.passwordId)
                ).build()
            )


        for (accountInfo in accountList) {

            // Build the presentation of the datasets
            val usernamePresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            usernamePresentation.setTextViewText(android.R.id.text1, accountInfo.username)
            val passwordPresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            passwordPresentation.setTextViewText(android.R.id.text1, "密码")

            val dataset = Dataset.Builder()
                .setValue(
                    this.parsedStructure.usernameId!!,
                    AutofillValue.forText(accountInfo.username),
                    usernamePresentation
                )
                .setValue(
                    this.parsedStructure.passwordId!!,
                    AutofillValue.forText(accountInfo.password),
                    passwordPresentation
                ).build()

            fillResponseBuilder.addDataset(dataset)
        }

        val fillResponse: FillResponse = fillResponseBuilder.build()

        // If there are no errors, call onSuccess() and pass the response
        callback.onSuccess(fillResponse)
    }

    /**
     * 获取账号密码
     */
    @SuppressLint("Range")
    fun fetchUserData(request: FillRequest): ArrayList<UserData> {
        var accountList = arrayListOf<UserData>()

        var cursor = MyDatabaseHelper(this).readDbByUrl(request.fillContexts[0].structure.activityComponent.packageName)

        if ( cursor.moveToFirst() ) {
            do {
                accountList.add( UserData( cursor.getString(cursor.getColumnIndex("account")), cursor.getString(cursor.getColumnIndex("passwd")) ) )
            } while (cursor.moveToNext())
        }
        /*
        val username = parsedStructure.usernameId.let { id ->
            id?.toString() ?: ""
        }

        val password = parsedStructure.passwordId.let { id ->
            id?.toString() ?: ""
        }
        */

        return accountList
    }

    /**
     * 解析Structure
     */
    fun parseStructure(structure: AssistStructure) {
        Log.i("自动填充", "检测中...")
        var usernameId: AutofillId? = null
        var passwordId: AutofillId? = null

        /*
        val nodes = structure.windowNodeCount

        for (i in 0 until nodes) {
            val node = structure.getWindowNodeAt(i)
            val viewNode = node.rootViewNode

            if ( spotKeyword(viewNode, accountKeyword) ) {
                usernameId = viewNode.autofillId
                Log.i("自动填充", "账号输入框")
            }

            if ( spotKeyword(viewNode, passwdKeyword) ) {
                passwordId = viewNode.autofillId
                Log.i("自动填充", "密码输入框")
            }
        }
         */

        val windowNodes: List<AssistStructure.WindowNode> = structure.run { (0 until windowNodeCount).map { getWindowNodeAt(it) } }

        windowNodes.forEach { windowNode: AssistStructure.WindowNode ->
            val viewNode: AssistStructure.ViewNode? = windowNode.rootViewNode
            parseNode(viewNode)
        }

        //return ParsedStructure(usernameId, passwordId)
    }

    /**
     * 解析Node
     * 递归
     */
    fun parseNode(viewNode: AssistStructure.ViewNode?) {

        if ( spotKeyword(viewNode, accountKeyword) ) {
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
    fun spotKeyword( viewNode: AssistStructure.ViewNode?, keywordList : ArrayList<String> ) : Boolean {

        Log.d("自动填充", "classname :: ${viewNode?.className.toString()}, ${EditText::class.java.name.toString()}")

        if ( viewNode?.className.equals(EditText::class.java.name) || viewNode?.className.equals(TextView::class.java.name) ) {
            Log.d("自动填充", " ${viewNode?.id.toString()} :: ${viewNode?.hint.toString()},  ${viewNode?.text.toString()}")

            for (word in keywordList) {
                if ( viewNode?.autofillHints?.contains(word) == true ) {
                    Log.i("自动填充", "检测到")
                    return true
                } else if ( viewNode?.id.toString().contains(word) ) {
                    Log.i("自动填充", "检测到")
                    return true
                } else if ( viewNode?.hint.toString().contains(word) ) {
                    Log.i("自动填充", "检测到")
                    return true
                } else if ( viewNode?.text.toString().contains(word) ) {
                    Log.i("自动填充", "检测到")
                    return true
                }
            }
        }

        return false
    }

    fun getAccountInfo() {

    }

    /**
     * 保存新账号密码
     */
    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        // Get the structure from the request
        val context: List<FillContext> = request.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure

        // Traverse the structure looking for data to save
        traverseStructure(structure)

        // Persist the data, if there are no errors, call onSuccess()
        callback.onSuccess()
    }

    fun traverseStructure(structure: AssistStructure) {
        val windowNodes: List<AssistStructure.WindowNode> = structure.run { (0 until windowNodeCount).map { getWindowNodeAt(it) } }

        windowNodes.forEach { windowNode: AssistStructure.WindowNode ->
            val viewNode: AssistStructure.ViewNode? = windowNode.rootViewNode
            traverseNode(viewNode)
        }
    }

    fun traverseNode(viewNode: AssistStructure.ViewNode?) {
        if (viewNode?.autofillHints?.isNotEmpty() == true) {
            // If the client app provides autofill hints, you can obtain them using:
            // viewNode.getAutofillHints();
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