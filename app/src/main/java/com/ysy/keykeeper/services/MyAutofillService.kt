package com.ysy.keykeeper.services

import android.R
import android.app.assist.AssistStructure
import android.content.Context
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
class MyAutofillService : AutofillService() {

    data class ParsedStructure(var usernameId: AutofillId, var passwordId: AutofillId)

    data class UserData(var username: String, var password: String)

    override fun onFillRequest(request: FillRequest, cancellationSignal: CancellationSignal, callback: FillCallback) {
        Log.i("无障碍", "填充请求!!!!!")
        /*
        // Get the structure from the request
        val context: List<FillContext> = request.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure

        // Traverse the structure looking for nodes to fill out.
        val parsedStructure: ParsedStructure = parseStructure(structure)

        // Fetch user data that matches the fields.
        val (username: String, password: String) = fetchUserData(parsedStructure)

        // Build the presentation of the datasets
        val usernamePresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
        usernamePresentation.setTextViewText(android.R.id.text1, "my_username")
        val passwordPresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
        passwordPresentation.setTextViewText(android.R.id.text1, "Password for my_username")

        // Add a dataset to the response
        val fillResponse: FillResponse = FillResponse.Builder()
            .addDataset(Dataset.Builder()
                .setValue(
                    parsedStructure.usernameId,
                    AutofillValue.forText(username),
                    usernamePresentation
                )
                .setValue(
                    parsedStructure.passwordId,
                    AutofillValue.forText(password),
                    passwordPresentation
                )
                .build())
            .build()

        // If there are no errors, call onSuccess() and pass the response
        callback.onSuccess(fillResponse)

        // Builder object requires a non-null presentation.
        val notUsed = RemoteViews(packageName, R.layout.simple_list_item_1)
         */

        /*
        val fillResponse: FillResponse = FillResponse.Builder()
            .addDataset(
                Dataset.Builder()
                    .setValue(parsedStructure.usernameId, null, notUsed)
                    .setValue(parsedStructure.passwordId, null, notUsed)
                    .build()
            )
            .setSaveInfo(
                SaveInfo.Builder(
                    SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                    arrayOf(parsedStructure.usernameId, parsedStructure.passwordId)
                ).build()
            )
            .build()
         */
    }

    fun parseStructure(structure: AssistStructure): ParsedStructure {
        var usernameId: AutofillId? = null
        var passwordId: AutofillId? = null

        val nodes = structure.windowNodeCount
        for (i in 0 until nodes) {
            val node = structure.getWindowNodeAt(i)
            val viewNode = node.rootViewNode

            if (viewNode.autofillHints?.contains("username") == true) {
                usernameId = viewNode.autofillId
            }

            if (viewNode.autofillHints?.contains("password") == true) {
                passwordId = viewNode.autofillId
            }
        }

        return ParsedStructure(usernameId!!, passwordId!!)
    }

    fun fetchUserData(parsedStructure: ParsedStructure): Pair<String, String> {
        val username = parsedStructure.usernameId.let { id ->
            id?.toString() ?: ""
        }

        val password = parsedStructure.passwordId.let { id ->
            id?.toString() ?: ""
        }

        return Pair(username, password)
    }



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
        val windowNodes: List<AssistStructure.WindowNode> =
            structure.run {
                (0 until windowNodeCount).map { getWindowNodeAt(it) }
            }

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

        val children: List<AssistStructure.ViewNode>? =
            viewNode?.run {
                (0 until childCount).map { getChildAt(it) }
            }

        children?.forEach { childNode: AssistStructure.ViewNode ->
            traverseNode(childNode)
        }
    }

}