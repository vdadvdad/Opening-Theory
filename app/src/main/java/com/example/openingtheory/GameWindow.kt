package com.example.openingtheory

import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import kotlin.concurrent.thread


class GameWindow : AppCompatActivity() {
    private var displayHeight = 0
    private var displayWidth = 0
    private var knightsPos = emptyArray<String>(); private var bishopsPos = emptyArray<String>()
    private var rooksPos = emptyArray<String>(); private var queensPos = emptyArray<String>()
    private var pawnsPos = emptyArray<String>(); private var kingsPos = emptyArray<String>()
    private var pos = emptyArray<String>(); private var isBlackToMove = false
    private var enPassant = ""
    private var squares = emptyArray<ImageButton>()
    private var square1 = ""; private var square2 = ""
    private var courseData = emptyList<String>()
    private var stepInfo = emptyList<String>()
    private var playerCanMove = false; var moveList = emptyArray<String>()
    private var canContinue = true
    override fun onCreate(savedInstanceState: Bundle?) {
        var courseStepNumber = 0
        val courseName = intent.getStringExtra("course_name")
        startCourse(courseName.toString())
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val gwLayout = ConstraintLayout(this)
        val displayMetrics: DisplayMetrics = this.resources.displayMetrics
        displayHeight = displayMetrics.heightPixels
        displayWidth = displayMetrics.widthPixels
        var scrollListOfMoves = HorizontalScrollView(this).apply{
            y = displayHeight*0.15f
            setBackgroundColor(resources.getColor(R.color.black))
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,  displayHeight/20)



        }
        val scrollGridLayout = LinearLayout(this).apply{
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,  displayHeight/20)


        }
        scrollListOfMoves.addView(scrollGridLayout)


        var textMessage = TextView(this).apply{
            setBackgroundColor(resources.getColor(R.color.black))
            text = courseData[0].split(": ")[1]
            width = displayWidth
            height = (displayHeight*0.15).toInt()
            //y = displayMetrics.heightPixels*0.05f
            setTextColor(resources.getColor(R.color.white))
            gravity = Gravity.RIGHT
            //textSize = displayWidth*0.025f
            setTypeface(null, Typeface.BOLD)
        }
        val continueButton = Button(this).apply{
            x = displayWidth/10f*7
            y = displayHeight/10f*7
            width = displayWidth/10*3
            height = displayHeight/10
            text = "Start"
            setTextColor(resources.getColor(R.color.black))
            gravity = Gravity.CENTER
            setBackgroundColor(resources.getColor(R.color.green))
            setOnClickListener(View.OnClickListener {
                Log.d("Listener", "Continue Button Listener")
                text = "Continue"
                if (canContinue){
                    courseStepNumber+=1
                stepInfo = courseData[courseStepNumber].split(" ")
                when (stepInfo[0]){
                    "play-move:" ->{
                        canContinue = false
                        thread{
                            for (i in 1 until stepInfo.size){
                                runOnUiThread{
                                    changePosition(stepInfo[i][0].toString()+stepInfo[i][1], stepInfo[i][2].toString()+stepInfo[i][3])
                                    createMoveButton(stepInfo[i][0].toString()+stepInfo[i][1], stepInfo[i][2].toString()+stepInfo[i][3], scrollGridLayout)
                                }

                                Thread.sleep(500)
                            }
                            canContinue = true
                        }


                    }
                    "text:" -> {
                        var txt = ""
                        for (i in 1 until stepInfo.size) txt+=stepInfo[i]+" "
                        textMessage.text = txt
                        courseStepNumber+=1
                    }
                }
                if (stepInfo[0][0]=='.') {
                    playerCanMove = true
                    canContinue = false
                    for (i in 1..Character.getNumericValue(stepInfo[0][1])){
                        moveList+=courseData[courseStepNumber+i].split(":")[0]
                    }
                    courseStepNumber+=Character.getNumericValue(stepInfo[0][1])
                }
                }

            })
        }

        val grid = GridLayout(this).apply{
            columnCount=8
            rowCount=8
            x = displayWidth/10f
            y = displayHeight*0.2f

        }
        gwLayout.addView(grid)
        gwLayout.addView(continueButton)
        var sq = "h9"
        for (i in 0..63) {
            sq = nextSquare(sq)
            if ((i%2==1 && sq[1].code%2==0) or (i%2==0 && sq[1].code%2==1)) {
                addSquare(this, R.drawable.black_square, sq, displayMetrics.widthPixels/10, scrollGridLayout)

            }
            else{
                addSquare(this, R.drawable.white_square, sq, displayMetrics.widthPixels/10, scrollGridLayout)

            }
        }
        if (isBlackToMove) squares.reverse()
        for (i in 0..63){
            grid.addView(squares[i])
        }
        startGame()
        gwLayout.addView(textMessage)
        gwLayout.addView(scrollListOfMoves)
        setContentView(gwLayout)
    }
    private fun startCourse(courseName: String){
        var filename = ""
        when (courseName){
            "Stafford Gambit" -> filename="stafford.txt"
        }
        courseData = application.assets.open(filename).bufferedReader().readText().split("\n")
    }
    private fun createMoveButton(square1: String, square2: String, scrollGridLayout: LinearLayout){
        val button = Button(this).apply{
            setBackgroundResource(R.drawable.rounded_rectangle)
            width = displayWidth/5
            height = displayHeight/20
            gravity = Gravity.CENTER
            setTextColor(resources.getColor(R.color.white))
            text = "$square1 $square2"
        }
        scrollGridLayout.addView(button)
    }
    private fun nextSquare(square: String): String{
        var sq = square
        when {
            sq[0]=='a' -> sq="b"+sq[1]
            sq[0]=='b' -> sq="c"+sq[1]
            sq[0]=='c' -> sq="d"+sq[1]
            sq[0]=='d' -> sq="e"+sq[1]
            sq[0]=='e' -> sq="f"+sq[1]
            sq[0]=='f' -> sq="g"+sq[1]
            sq[0]=='g' -> sq="h"+sq[1]
            sq[0]=='h' -> sq="a"+(sq[1].code-1).toChar()
        }
        return sq
    }
    private fun startCustomGame(fen: String){
        clearPosition()
        var sq = "h9"
        val fen = fen.split(" ").toTypedArray()
        for (i in fen[0]){
            if (i.isDigit()){
                for (k in 1..Character.getNumericValue(i)) sq = nextSquare(sq)
            }
            else{
                when (i){
                    'n' -> knightsPos+="B$sq"
                    'N' -> knightsPos+="W$sq"
                    'b' -> bishopsPos+="B$sq"
                    'B' -> bishopsPos+="W$sq"
                    'r' -> rooksPos+="B$sq"
                    'R' -> rooksPos+="W$sq"
                    'q' -> queensPos+="B$sq"
                    'Q' -> queensPos+="W$sq"
                    'k' -> kingsPos+="B$sq"
                    'K' -> kingsPos+="W$sq"
                }
                sq = nextSquare(sq)
            }
        }
        pos+=knightsPos+bishopsPos+rooksPos+queensPos+kingsPos
        if (fen[1]=="b") isBlackToMove = true
        if (fen[3]!="-") enPassant = fen[3]
        setupPosition()
    }
    private fun clearPosition(){
        knightsPos = emptyArray()
        bishopsPos = emptyArray()
        queensPos = emptyArray()
        kingsPos = emptyArray()
        pawnsPos = emptyArray()
        pos = emptyArray()
    }
    private fun addSquare (context: GameWindow, id: Int, sq:String, side: Int, scrollGridLayout: LinearLayout){
        squares+=(ImageButton(this).apply{
            setBackgroundResource(id)
            tag = sq
            adjustViewBounds = true
            setPadding(10)
            layoutParams = ViewGroup.LayoutParams(side, side)
            setOnClickListener(View.OnClickListener {
                val id = this.tag

                if (playerCanMove){
                    when{
                    square1 == "" ->{
                        square1=id.toString()
                        Log.d("initialSquare", "initial square$square1")
                        if (("B$square1" !in pos)&& isBlackToMove) square1=""
                        if (("W$square1" !in pos) && !isBlackToMove) square1=""
                        Log.d("info1", "square1 changed to $square1")
                    }
                    square2 == "" ->{
                        square2 = id.toString()
                        Log.d("info2", "square2 changed to $square2")
                        if (square1!=square2 && isValidMove(square1, square2) && square1+square2 in moveList){
                            changePosition(square1, square2)

                            playerCanMove = false
                            canContinue = true
                        }
                        square1 = ""
                        square2 = ""
                    }
                    }
                }
            })
        })
    }
    fun remove(array: Array<String>, index: Int): Array<String>{
        var arr = emptyArray<String>()
        for (i in array.indices) if (i!=index) arr+=array[i]
        return arr
    }
    private fun isKingMove(column1: Int, row1: Int, column2: Int, row2: Int): Boolean{
        val columns = "abcdefgh"
        val s = if (isBlackToMove) "B"+columns[column2-1].toString()+row2.toString()
        else "W"+columns[column2-1].toString()+row2.toString()
        if (s in pos) return false
        return ((column1==column2 || column1-1==column2 || column1+1==column2) && (row1==row2 || row1+1==row2 || row1-1==row2))
    }
    private fun isPawnMove(column1: Int, row1: Int, column2: Int, row2: Int, s: String): Boolean{
        Log.d("pawnMove", "$s; $row1, $row2; $column1, $column2")
        val columns = "abcdefgh"
        val c = if (isBlackToMove) "B"+columns[column2-1].toString()+row2.toString()
        else "W"+columns[column2-1].toString()+row2.toString()
        if (c in pos) return false
        Log.d("check passed", "check 'not to capture same color' completed")

        if (s=="W" && row1+1==row2 && column1==column2 && (("W"+columns[column2-1]+row2.toString() !in pos) && ("B"+columns[column2-1]+row2.toString() !in pos))){
            enPassant = ""
            return true
        }
        if (s=="W" && row1==2 && row2==4 && column1==column2 && (("W"+columns[column2-1]+row2.toString() !in pos) && ("B"+columns[column2-1]+row2.toString() !in pos))){
            enPassant = columns[column1-1].toString()+"3"
            return true
        }
        if (s=="B" && row1-1==row2 && column1==column2 && (("W"+columns[column2-1]+row2.toString() !in pos) && ("B"+columns[column2-1]+row2.toString() !in pos))){
            enPassant = ""
            return true
        }
        if (s=="B" && row1==7 && row2==5 && column1==column2 && (("W"+columns[column2-1]+row2.toString() !in pos) && ("B"+columns[column2-1]+row2.toString() !in pos))){
            enPassant = columns[column1-1].toString()+"6"
            return true
        }
        if ((row1+1==row2 && column1-1==column2 && s=="W") or (row1+1==row2 && column1+1==column2 && s=="W")
            or (row1-1==row2 && column1+1==column2 && s=="B") or (row1-1==row2 && column1-1==column2 && s=="B")){
            Log.d("pawn capture", enPassant+" "+columns[column2-1]+row2.toString())
            var s2="W"
            if (s=="W") s2="B"
            for (i in pos){
                if (i==s2+columns[column2-1]+row2.toString()){
                    enPassant = ""
                    return true
                }
            }
            if (enPassant!=""){
                val q = if(s2=="W") 1
                else -1
                if (columns[column2-1]+row2.toString()==enPassant){
                    for (i in pawnsPos.indices){
                        Log.d("pawn", pawnsPos[i]+" "+s2+columns[column2-1]+(row2+q).toString())
                        if (pawnsPos[i]==s2+columns[column2-1]+(row2+q).toString()){
                            Log.d("remove called", pawnsPos[i]+" "+s2+columns[column2-1]+(row2+q).toString())
                            pawnsPos = remove(pawnsPos, i)
                            Log.d("after remove", pawnsPos.contentToString())
                            val n = column2 - 1 + ((8 - row2-q) * 8)
                            Log.d("deleted", n.toString())
                            squares[n].setImageResource(0)
                            break
                        }
                    }
                    enPassant = ""
                    return true
                }
            }
        }
        Log.d("check passed", "this will return false")
        return false
    }
    private fun isKnightMove(column1: Int, row1: Int, column2: Int, row2: Int): Boolean{
        val columns = "abcdefgh"
        val s = if (isBlackToMove) "B"+columns[column2-1].toString()+row2.toString()
        else "W"+columns[column2-1].toString()+row2.toString()
        if (s in pos) return false
        if ((column1+2==column2 || column1-2==column2) && (row1+1==row2 || row1-1==row2)) return true
        if ((column1+1==column2 || column1-1==column2) && (row1+2==row2 || row1-2==row2)) return true
        return false
    }
    private fun isBishopMove(column1: Int, row1: Int, column2: Int, row2: Int): Boolean{
        val columns = "abcdefgh"
        if (kotlin.math.abs(column2 - column1) == kotlin.math.abs(row2 - row1)){
            if (column2>column1){
                if (row2>row1){
                    for (i in column1+1 until column2){
                        for (k in pos){
                            if (k[1]==columns[i] && k[2]==(row1+i-column1).toChar()) return false
                        }
                    }
                }
                else{
                    for (i in column1+1 until column2){
                        for (k in pos){
                            if (k[1]==columns[i] && k[2]==(row2-i+column1).toChar()) return false
                        }
                    }
                }
            }
            else{
                if (row2>row1){
                    for (i in column2+1 until column1){
                        for (k in pos){
                            if (k[1]==columns[i] && k[2]==(row2+i-column1).toChar()) return false
                        }
                    }
                }
                else{
                    for (i in column2+1 until column1){
                        for (k in pos){
                            if (k[1]==columns[i] && k[2]==(row2-i+column1).toChar()) return false
                        }
                    }
                }
            }
        }
        else return false
        val s = if (isBlackToMove) "B"+columns[column2-1].toString()+row2.toString()
        else "W"+columns[column2-1].toString()+row2.toString()
        if (s in pos) return false
        return true
    }
    private fun isRookMove(column1: Int, row1: Int, column2: Int, row2: Int): Boolean{
        val columns="abcdefgh"
        if ((column1==column2) or (row1==row2)){
            if (column1==column2){
                val startRow= kotlin.math.min(row1, row2)
                val endRow= kotlin.math.max(row1, row2)
                for (i in startRow+1 until endRow){
                    for (j in pos){
                        if (j[1]==columns[column1-1] && j[2]==(i.toChar())) return false
                    }
                }

            }
            val startColumn = kotlin.math.min(column1, column2)
            val endColumn = kotlin.math.max(column1, column2)
            for (i in startColumn+1 until endColumn){
                for (j in pos){
                    if (j[1]==columns[i] && j[2]==(row1.toChar())) return false
                }
            }
        }
        val s = if (isBlackToMove) "B"+columns[column2-1].toString()+row2.toString()
        else "W"+columns[column2-1].toString()+row2.toString()
        if (s in pos) return false
        return true
    }
    private fun setupPosition(){
        var column=square2Table(knightsPos[0][1].toString()+knightsPos[0][2])
        squares[column-1+((8-Character.getNumericValue(knightsPos[0][2]))*8)].setImageResource(R.drawable.black_knight)
        column=square2Table(knightsPos[1][1].toString()+knightsPos[1][2])
        squares[column-1+((8-Character.getNumericValue(knightsPos[1][2]))*8)].setImageResource(R.drawable.black_knight)
        column=square2Table(knightsPos[2][1].toString()+knightsPos[2][2])
        squares[column-1+((8-Character.getNumericValue(knightsPos[2][2]))*8)].setImageResource(R.drawable.white_knight)
        column=square2Table(knightsPos[3][1].toString()+knightsPos[3][2])
        squares[column-1+((8-Character.getNumericValue(knightsPos[3][2]))*8)].setImageResource(R.drawable.white_knight)
        //
        column=square2Table(bishopsPos[0][1].toString()+bishopsPos[0][2])
        squares[column-1+((8-Character.getNumericValue(bishopsPos[0][2]))*8)].setImageResource(R.drawable.black_bishop)
        column=square2Table(bishopsPos[1][1].toString()+bishopsPos[1][2])
        squares[column-1+((8-Character.getNumericValue(bishopsPos[1][2]))*8)].setImageResource(R.drawable.black_bishop)
        column=square2Table(bishopsPos[2][1].toString()+bishopsPos[2][2])
        squares[column-1+((8-Character.getNumericValue(bishopsPos[2][2]))*8)].setImageResource(R.drawable.white_bishop)
        column=square2Table(bishopsPos[3][1].toString()+bishopsPos[3][2])
        squares[column-1+((8-Character.getNumericValue(bishopsPos[3][2]))*8)].setImageResource(R.drawable.white_bishop)
        //
        column=square2Table(rooksPos[0][1].toString()+rooksPos[0][2])
        squares[column-1+((8-Character.getNumericValue(rooksPos[0][2]))*8)].setImageResource(R.drawable.black_rook)
        column=square2Table(rooksPos[1][1].toString()+rooksPos[1][2])
        squares[column-1+((8-Character.getNumericValue(rooksPos[1][2]))*8)].setImageResource(R.drawable.black_rook)
        column=square2Table(rooksPos[2][1].toString()+rooksPos[2][2])
        squares[column-1+((8-Character.getNumericValue(rooksPos[2][2]))*8)].setImageResource(R.drawable.white_rook)
        column=square2Table(rooksPos[3][1].toString()+rooksPos[3][2])
        squares[column-1+((8-Character.getNumericValue(rooksPos[3][2]))*8)].setImageResource(R.drawable.white_rook)
        //
        column=square2Table(queensPos[0][1].toString()+queensPos[0][2])
        squares[column-1+((8-Character.getNumericValue(queensPos[0][2]))*8)].setImageResource(R.drawable.black_queen)
        column=square2Table(queensPos[1][1].toString()+queensPos[1][2])
        squares[column-1+((8-Character.getNumericValue(queensPos[1][2]))*8)].setImageResource(R.drawable.white_queen)
        //
        column=square2Table(kingsPos[0][1].toString()+kingsPos[0][2])
        squares[column-1+((8-Character.getNumericValue(kingsPos[0][2]))*8)].setImageResource(R.drawable.black_king)
        column=square2Table(kingsPos[1][1].toString()+kingsPos[1][2])
        squares[column-1+((8-Character.getNumericValue(kingsPos[1][2]))*8)].setImageResource(R.drawable.white_king)
        for (i in 1..8){
            squares[i-1+((8-2)*8)].setImageResource(R.drawable.white_pawn)
            squares[i-1+((8-7)*8)].setImageResource(R.drawable.black_pawn)
        }
    }
    private fun isValidMove (square1: String, square2: String) : Boolean{
        var column1=square2Table(square1)
        var row1=Character.getNumericValue(square1[1])
        var column2=square2Table(square2)
        var row2=Character.getNumericValue(square2[1])
        var s = "W"
        if (isBlackToMove) s = "B"
        if (s+square1 in knightsPos){
            if (isKnightMove(column1, row1,column2,row2)) enPassant = ""; return true
        }
        else if (s+square1 in bishopsPos){
            if (isBishopMove(column1, row1, column2, row2)) enPassant = ""; return true
        }
        else if (s+square1 in rooksPos){
            if (isRookMove(column1, row1, column2, row2)) enPassant = ""; return true
        }
        else if (s+square1 in queensPos){
            if (isBishopMove(column1, row1, column2, row2) or isRookMove(column1, row1, column2, row2)) enPassant = ""; return true
        }
        else if (s+square1 in pawnsPos){
            if (isPawnMove(column1, row1, column2, row2, s)) return true
        }
        else if (s+square1 in kingsPos){
            if (isKingMove(column1, row1, column2, row2)) enPassant = ""; return true
        }
        return false
    }
    private fun startGame(){
        knightsPos+="Bb8"; knightsPos+="Bg8"; knightsPos+="Wb1"; knightsPos+="Wg1"
        bishopsPos+="Bc8"; bishopsPos+="Bf8"; bishopsPos+="Wc1"; bishopsPos+="Wf1"
        rooksPos+="Ba8"; rooksPos+="Bh8"; rooksPos+="Wa1"; rooksPos+="Wh1"
        queensPos+="Bd8"; queensPos+="Wd1"
        for (i in charArrayOf('a','b','c', 'd','e','f', 'g', 'h')){
            pawnsPos+="B"+i+"7"; pawnsPos+="W"+i+"2"
            pos+="B"+i+"8"; pos+="B"+i+"7"; pos+="W"+i+"1"; pos+="W"+i+"2"
        }
        kingsPos+="Be8"; kingsPos+="We1"
        setupPosition()
    }
    private fun square2Table(square: String): Int {
        var column = 0
        when{
            square[0]=='h' -> column = 8
            square[0]=='g' -> column = 7
            square[0]=='f' -> column = 6
            square[0]=='e' -> column = 5
            square[0]=='d' -> column = 4
            square[0]=='c' -> column = 3
            square[0]=='b' -> column = 2
            square[0]=='a' -> column = 1
        }
        return column
    }
    private fun changePosition(square1: String, square2: String){
        Log.d("thread", Thread.currentThread().name)
        var nSquare1 = ""
        var nSquare2 = ""
        if (isBlackToMove) {
            nSquare1 = "B$square1"
            nSquare2 = "W$square2"
        }
        else{
            nSquare1 = "W$square1"
            nSquare2 = "B$square2"
        }
        Log.d("changeposition called", "$square1, $square2")
        when (nSquare2) {
            in knightsPos -> {
                for (i in knightsPos.indices){
                    if (knightsPos[i][1]==square2[0] && knightsPos[i][1]==square2[1]){
                        knightsPos = remove(knightsPos, i)
                        break
                    }

                }
            }
            in bishopsPos -> {
                for (i in bishopsPos.indices){
                    if (bishopsPos[i][1]==square2[0] && bishopsPos[i][2]==square2[1]){
                        bishopsPos = remove(bishopsPos, i)
                        break
                    }

                }
            }
            in rooksPos -> {
                for (i in rooksPos.indices){
                    if (rooksPos[i][1]==square2[0] && rooksPos[i][2]==square2[1]){
                        rooksPos = remove(rooksPos, i)
                        break
                    }

                }
            }
            in queensPos -> {
                for (i in queensPos.indices){
                    if (queensPos[i][1]==square2[0] && queensPos[i][2]==square2[1]){
                        queensPos = remove(queensPos, i)
                        break
                    }

                }
            }
            in pawnsPos -> {
                for (i in pawnsPos.indices){
                    if (pawnsPos[i][1]==square2[0] && pawnsPos[i][2]==square2[1]){
                        pawnsPos = remove(pawnsPos, i)
                        break
                    }

                }
            }
            in kingsPos -> {
                for (i in kingsPos.indices){
                    if (kingsPos[i][1]==square2[0] && kingsPos[i][2]==square2[1]){
                        kingsPos = remove(kingsPos, i)
                        break
                    }

                }
            }
        }
        for (i in pos.indices){
            if (pos[i][1]==square2[0] && pos[i][2]==square2[1]){
                pos = remove(pos, i)
                break
            }

        }
        when (nSquare1) {
            in knightsPos -> {
                for (i in knightsPos.indices) {
                    if (knightsPos[i][1] == square1[0] && knightsPos[i][2] == square1[1]) {
                        knightsPos = remove(knightsPos, i)
                        knightsPos += if (isBlackToMove) "B$square2"
                        else "W$square2"
                        val column1 = square2Table(square1)
                        val column2 = square2Table(square2)
                        val n = column1 - 1 + ((8 - Character.getNumericValue(square1[1])) * 8)
                        squares[n].setImageResource(0)
                        if (isBlackToMove) squares[column2 - 1 + ((8 - Character.getNumericValue(square2[1])) * 8)].setImageResource(
                            R.drawable.black_knight
                        )
                        else squares[column2 - 1 + ((8 - Character.getNumericValue(square2[1])) * 8)].setImageResource(
                            R.drawable.white_knight
                        )
                    }

                }
            }
            in bishopsPos -> {
                for (i in bishopsPos.indices){
                    if (bishopsPos[i][1]==square1[0] && bishopsPos[i][2]==square1[1]){
                        bishopsPos = remove(bishopsPos, i)
                        bishopsPos += if (isBlackToMove) "B$square2"
                        else "W$square2"
                        val column1=square2Table(square1)
                        val column2=square2Table(square2)
                        val n = column1-1+((8-Character.getNumericValue(square1[1]))*8)
                        squares[n].setImageResource(0)
                        if (isBlackToMove) squares[column2-1+((8-Character.getNumericValue(square2[1]))*8)].setImageResource(R.drawable.black_bishop)
                        else squares[column2-1+((8-Character.getNumericValue(square2[1]))*8)].setImageResource(R.drawable.white_bishop)
                    }

                }
            }
            in rooksPos -> {
                for (i in rooksPos.indices){
                    if (rooksPos[i][1]==square1[0] && rooksPos[i][2]==square1[1]){
                        rooksPos = remove(rooksPos, i)
                        rooksPos += if (isBlackToMove)"B$square2"
                        else "W$square2"
                        val column1=square2Table(square1)
                        val column2=square2Table(square2)
                        val n = column1-1+((8-Character.getNumericValue(square1[1]))*8)
                        squares[n].setImageResource(0)
                        if (isBlackToMove) squares[column2-1+((8-Character.getNumericValue(square2[1]))*8)].setImageResource(R.drawable.black_rook)
                        else squares[column2-1+((8-Character.getNumericValue(square2[1]))*8)].setImageResource(R.drawable.white_rook)
                    }

                }
            }
            in queensPos -> {
                for (i in queensPos.indices){
                    if (queensPos[i][1]==square1[0] && queensPos[i][2]==square1[1]){
                        queensPos = remove(queensPos, i)
                        queensPos+= if (isBlackToMove) "B$square2"
                        else "W$square2"
                        val column1=square2Table(square1)
                        val column2=square2Table(square2)
                        val n = column1-1+((8-Character.getNumericValue(square1[1]))*8)
                        squares[n].setImageResource(0)
                        if (isBlackToMove) squares[column2-1+((8-Character.getNumericValue(square2[1]))*8)].setImageResource(R.drawable.black_queen)
                        else squares[column2-1+((8-Character.getNumericValue(square2[1]))*8)].setImageResource(R.drawable.white_queen)
                    }

                }
            }
            in pawnsPos -> {
                for (i in 0 until (pawnsPos.size)){
                    if (pawnsPos[i][1]==square1[0] && pawnsPos[i][2]==square1[1]){
                        pawnsPos = remove(pawnsPos, i)
                        pawnsPos+= if (isBlackToMove) "B$square2"
                        else "W$square2"
                        val column1=square2Table(square1)
                        val column2=square2Table(square2)
                        val n = column1-1+((8-Character.getNumericValue(square1[1]))*8)
                        squares[n].setImageResource(0)
                        if (isBlackToMove) squares[column2-1+((8-Character.getNumericValue(square2[1]))*8)].setImageResource(R.drawable.black_pawn)
                        else squares[column2-1+((8-Character.getNumericValue(square2[1]))*8)].setImageResource(R.drawable.white_pawn)
                    }

                }
            }
            in kingsPos -> {
                for (i in 0 until (kingsPos.size)){
                    if (kingsPos[i][1]==square1[0] && kingsPos[i][2]==square1[1]){
                        kingsPos = remove(kingsPos, i)
                        kingsPos+= if (isBlackToMove) "B$square2"
                        else "W$square2"
                        val column1=square2Table(square1)
                        val column2=square2Table(square2)
                        val n = column1-1+((8-Character.getNumericValue(square1[1]))*8)
                        squares[n].setImageResource(0)
                        if (isBlackToMove) squares[column2-1+((8-Character.getNumericValue(square2[1]))*8)].setImageResource(R.drawable.black_king)
                        else squares[column2-1+((8-Character.getNumericValue(square2[1]))*8)].setImageResource(R.drawable.white_king)

                    }

                }
            }

        }
        pos += if (isBlackToMove) "B$square2"
        else "W$square2"
        for (i in 0 until (pos.size)) {
            if (pos[i][1] == square1[0] && pos[i][2] == square1[1]) {
                pos = remove(pos, i)
                break
            }
        }
        isBlackToMove = !isBlackToMove
    }
}