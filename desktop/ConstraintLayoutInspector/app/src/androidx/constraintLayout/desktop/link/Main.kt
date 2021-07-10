/*
* Copyright 2021 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package androidx.constraintLayout.desktop.link

import androidx.constraintLayout.desktop.link.LayoutView.Companion.showLayoutView
import androidx.constraintLayout.desktop.scan.CLTreeNode
import androidx.constraintLayout.desktop.scan.SyntaxHighlight
import androidx.constraintLayout.desktop.utils.Desk
import androidx.constraintlayout.core.parser.CLElement
import androidx.constraintlayout.core.parser.CLParser
import androidx.constraintlayout.core.parser.CLParsingException
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class Main internal constructor() : JPanel(BorderLayout()) {
    var motionLink = MotionLink()
    var mMainText = JTextPane()
    var mMessages = JLabel()
    var layoutListTree = JTree()
    var scrollPaneList = JScrollPane(layoutListTree)
    var highlight = SyntaxHighlight(mMainText)
    var mMainTextScrollPane = JScrollPane(mMainText)
    var mSlider = JSlider()
    var drawDebug = false
    var layoutView: LayoutView? = null

    init {
        val getButton = JButton("Get")
        val connectButton = JButton("Connect")
        val sendButton = JButton("Send")
        val resetProgressButton = JButton("Reset Progress")
        val toggleDrawDebug = JButton("Toggle Debug")
        val showLayout = JButton("Show Layout")
        val formatText = JButton("Format Text")
        mMessages.horizontalAlignment = SwingConstants.RIGHT
        val font = Font("Courier", Font.PLAIN, 20)
        mMainText.font = font
        scrollPaneList.preferredSize = Dimension(200, 100)
        val northPanel = JPanel()
        northPanel.add(connectButton)
        northPanel.add(toggleDrawDebug)
        northPanel.add(showLayout)
        northPanel.add(getButton)
        northPanel.add(sendButton)
        northPanel.add(formatText)
        val southPanel = JPanel(BorderLayout())
        southPanel.add(mSlider, BorderLayout.CENTER)
        southPanel.add(resetProgressButton, BorderLayout.EAST)
        southPanel.add(mMessages, BorderLayout.SOUTH)
        add(northPanel, BorderLayout.NORTH)
        add(scrollPaneList, BorderLayout.WEST)
        add(mMainTextScrollPane, BorderLayout.CENTER)
        add(southPanel, BorderLayout.SOUTH)
        mSlider.value = 0
        motionLink.addListener { event: MotionLink.Event, link: MotionLink ->
            fromLink(
                event,
                link
            )
        }
        layoutListTree.selectionModel.addTreeSelectionListener(TreeSelectionListener { e ->
            val path = e.path
            println(path)
            if (path.pathCount > 2) {
                val selected = path.lastPathComponent as CLTreeNode
                println("selected " + selected.mKeyStart + "," + selected.mKeyEnd)
                mMainText.select(selected.mKeyStart, selected.mKeyEnd + 1)
                mMainText.requestFocus()
                return@TreeSelectionListener
            }
            val root =
                path.getPathComponent(0) as DefaultMutableTreeNode
            val selected =
                path.getPathComponent(1) as DefaultMutableTreeNode
            val index = root.getIndex(selected)
            motionLink.selectMotionScene(index)
            motionLink.getContent()
        })
        motionLink.getLayoutList()
        mMessages.text = "ok"
        connectButton.addActionListener { e: ActionEvent? -> motionLink.getLayoutList() }
        toggleDrawDebug.addActionListener { e: ActionEvent? ->
            motionLink.setDrawDebug(
                !drawDebug.also { drawDebug = it }
            )
        }
        showLayout.addActionListener { e: ActionEvent? -> motionLink.getLayoutList() }
        resetProgressButton.addActionListener { e: ActionEvent? ->
            motionLink.sendProgress(
                Float.NaN
            )
        }
        getButton.addActionListener { e: ActionEvent? -> motionLink.getContent() }
        mSlider.addChangeListener { e: ChangeEvent? ->
            motionLink.sendProgress(
                mSlider.value / 100f
            )
        }
        formatText.addActionListener {
            try {
                mMainText.text = formatJson(mMainText.text)
                updateTree()
            } catch (e : Exception) {
            }
        }

        mMainText.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                if (highlight.update) {
                    return
                }
                motionLink.sendContent(mMainText.text)
            }

            override fun removeUpdate(e: DocumentEvent) {
                if (highlight.update) {
                    return
                }
                motionLink.sendContent(mMainText.text)
            }

            override fun changedUpdate(e: DocumentEvent) {
                if (highlight.update) {
                    return
                }
                motionLink.sendContent(mMainText.text)
            }
        })
    }

    interface DesignSurfaceModification {
        fun getElement(name: String) : CLElement?
        fun updateElement(name: String, content: CLElement)
    }

    private fun fromLink(event: MotionLink.Event, link: MotionLink) {
        when (event) {
            MotionLink.Event.ERROR -> {
                mMessages.text = link.errorMessage
                mMessages.foreground = Color.RED.darker()
                link.errorMessage = ""
            }
            MotionLink.Event.STATUS -> {
                mMessages.text = link.statusMessage
                mMessages.foreground = Color.BLACK
                link.errorMessage = ""
            }
            MotionLink.Event.LAYOUT_UPDATE -> {
                if (layoutView == null) {
                    layoutView = showLayoutView(link, object: DesignSurfaceModification {
                        override fun getElement(name: String): CLElement? {
                            val json = CLParser.parse(mMainText.text)
                            return json.get(name)
                        }

                        override fun updateElement(name: String, content: CLElement) {
                            val json = CLParser.parse(mMainText.text)
                            json.put(name, content)
                            mMainText.text = json.toFormattedJSON(0, 2)
                            layoutView?.setModel(CLParser.parse(mMainText.text))
                        }
                    })
                    link.setUpdateLayoutPolling(true)
                }
                layoutView!!.setLayoutInformation(link.layoutInfos)
            }
            MotionLink.Event.LAYOUT_LIST_UPDATE -> {
                val root = DefaultMutableTreeNode("root")
                val model = DefaultTreeModel(root)
                var i = 0
                while (i < link.layoutNames.size) {
                    root.add(DefaultMutableTreeNode(link.layoutNames[i]))
                    i++
                }
                layoutListTree.isRootVisible = false
                layoutListTree.model = model
            }
            MotionLink.Event.MOTION_SCENE_UPDATE -> {
                try {
                    mMainText.text = formatJson(link.motionSceneText)
                    layoutView?.setModel(CLParser.parse(mMainText.text))
                } catch (e : Exception) {
                    mMainText.text = link.motionSceneText
                }
                updateTree()
            }
        }
    }

    private fun formatJson(text: String) : String {
        val json = CLParser.parse(text)
        var indentation = 2
        if (json.has("ConstraintSets")) {
            indentation = 3
        }
        return json.toFormattedJSON(0, indentation)
    }

    private fun updateTree() {
        val model = layoutListTree.model as DefaultTreeModel
        val root = model.root as DefaultMutableTreeNode
        val n = root.childCount
        for (i in 0 until n) {
            val child =
                root.getChildAt(i) as DefaultMutableTreeNode
            child.removeAllChildren()
            if (motionLink.mSelectedIndex == i) {
                println(i)
                try {
                    CLTreeNode.parse(motionLink.motionSceneText, child)
                } catch (e: CLParsingException) {
                    mMessages.text = e.message
                    mMessages.foreground = Color.RED.darker()
                }
            }
        }
        model.reload()
        layoutListTree.expandRow(motionLink.mSelectedIndex)
    }

    var myTmpFile: File? = null
    var myTempLastModified: Long = 0
    var myTmpTimer: Timer? = null

    fun remoteEditStop() {
        myTmpTimer!!.stop()
        myTmpFile!!.deleteOnExit()
        myTmpFile = null
    }

    fun remoteEdit() {
        try {
            val tmp = File.createTempFile(motionLink.selectedLayoutName, ".json5")
            val fw = FileWriter(tmp)
            fw.write(motionLink.motionSceneText)
            fw.close()
            myTempLastModified = tmp.lastModified()
            Desktop.getDesktop().open(tmp)
            myTmpFile = tmp;
            myTmpTimer = Timer(500, ActionListener { e: ActionEvent? -> checkForUpdate() })
            myTmpTimer!!.isRepeats = true
            myTmpTimer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun checkForUpdate() {
        val lastM = myTmpFile!!.lastModified()
        if (lastM - myTempLastModified > 0) {
            try {
                myTempLastModified = lastM
                val fr = FileReader(myTmpFile)
                val buff = CharArray(myTmpFile!!.length().toInt())
                var off = 0
                while (true) {
                    val len = fr.read(buff, off, buff.size - off)
                    println(len)
                    if (len <= 0) break
                    off += len
                }
                fr.close()
                mMainText.text = String(buff, 0, off)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(str: Array<String>) {
            val frame = JFrame("ConstraintLayout Live Editor")
            val panel = Main()
            frame.contentPane = panel
            val main: Array<Action> = arrayOf<Action>(
                object : AbstractAction("File") {
                    override fun actionPerformed(e: ActionEvent) {}
                },
                object : AbstractAction("Link") {
                    override fun actionPerformed(e: ActionEvent) {
                        panel.remoteEdit()
                    }
                },
                object : AbstractAction("UnLink") {
                    override fun actionPerformed(e: ActionEvent) {
                        panel.remoteEditStop()
                    }
                }
            )
            frame.jMenuBar = Desk.createTopMenu(main)
            Desk.rememberPosition(frame, null)

            frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            frame.isVisible = true
        }
    }

}