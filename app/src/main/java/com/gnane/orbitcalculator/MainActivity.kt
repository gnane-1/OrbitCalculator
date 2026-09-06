package com.gnane.orbitcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import java.text.DecimalFormat

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState); enableEdgeToEdge()
  setContent { MaterialTheme { OrbitApp() } }
 }
}
private val Space=Color(0xFF05070B)
private val Card=Color(0xFF15171C)
private val Utility=Color(0xFF292A30)
private val Ink=Color(0xFFF2F1F4)
private val Muted=Color(0xFF92939A)
private val Orange=Color(0xFFFF8500)
private enum class Page { CALC,HISTORY,THEMES }
data class Item(val expression:String,val result:String)

@Composable fun OrbitApp() {
 var page by remember { mutableStateOf(Page.CALC) }
 var accent by remember { mutableStateOf(Orange) }
 var display by remember { mutableStateOf("0") }
 var expression by remember { mutableStateOf("") }
 var stored by remember { mutableStateOf<Double?>(null) }
 var pending by remember { mutableStateOf<String?>(null) }
 var fresh by remember { mutableStateOf(true) }
 var memory by remember { mutableStateOf(0.0) }
 var history by remember { mutableStateOf(listOf<Item>()) }
 fun number()=display.replace(",","").toDoubleOrNull()?:0.0
 fun fmt(v:Double)=if(v.isFinite()) DecimalFormat("#,##0.########").format(v) else "Error"
 fun equals() {
  val left=stored?:return
  val right=number()
  val op=pending?:""
  val result=when(op){"+"->left+right;"−"->left-right;"×"->left*right;"÷"->if(right==0.0)Double.NaN else left/right;else->right}
  val answer=fmt(result)
  val exp=expression.ifBlank { fmt(left)+" "+op+" "+fmt(right) }
  if(answer!="Error") history=listOf(Item(exp,answer))+history
  display=answer; expression=exp; stored=null; pending=null; fresh=true
 }
 fun tap(k:String) {
  when(k){
   "AC"->{display="0";expression="";stored=null;pending=null;fresh=true}
   "±"->if(display!="0") display=if(display.startsWith("-"))display.drop(1) else "-"+display
   "%"->display=fmt(number()/100)
   "mc"->memory=0.0
   "m+"->memory+=number()
   "m-"->memory-=number()
   "mr"->{display=fmt(memory);fresh=true}
   "+","−","×","÷"->{if(pending!=null&&!fresh)equals();stored=number();pending=k;expression=fmt(stored?:0.0)+" "+k;fresh=true}
   "="->equals()
   "."->if(fresh){display="0.";fresh=false}else if(!display.contains("."))display+="."
   else->{display=if(fresh||display=="0"||display=="Error")k else display+k;fresh=false;if(pending!=null)expression=fmt(stored?:0.0)+" "+pending+" "+display}
  }
 }
 Surface(Modifier.fillMaxSize(),color=Space){
  when(page){
   Page.CALC->CalcScreen(display,expression,accent,::tap,{page=Page.HISTORY},{page=Page.THEMES})
   Page.HISTORY->HistoryScreen(history,accent,{page=Page.CALC}){item->display=item.result;expression=item.expression;fresh=true;page=Page.CALC}
   Page.THEMES->ThemeScreen(accent,{accent=it},{page=Page.CALC})
  }
 }
}

@Composable private fun CalcScreen(display:String,expression:String,accent:Color,tap:(String)->Unit,onHistory:()->Unit,onThemes:()->Unit){
 Box(Modifier.fillMaxSize()){
  Canvas(Modifier.fillMaxWidth().height(390.dp)){
   drawCircle(brush=Brush.radialGradient(listOf(accent.copy(alpha=.30f),Color.Transparent),center=Offset(size.width*.72f,size.height*.05f),radius=size.width*.9f),radius=size.width*.9f,center=Offset(size.width*.72f,size.height*.05f))
   drawCircle(color=Color(0xFF11100F),radius=size.width*.60f,center=Offset(size.width*.83f,-size.height*.10f))
   drawCircle(color=accent.copy(alpha=.35f),radius=size.width*.605f,center=Offset(size.width*.83f,-size.height*.10f),style=Stroke(3.dp.toPx()))
  }
  Column(Modifier.fillMaxSize().padding(horizontal=22.dp,vertical=26.dp)){
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
    TopButton("◷",onHistory)
    Column(horizontalAlignment=Alignment.CenterHorizontally){Text("CALCULATE",color=Muted,fontSize=8.sp,letterSpacing=4.sp);Text("BEYOND NUMBERS",color=Muted,fontSize=7.sp,letterSpacing=3.sp);Spacer(Modifier.height(9.dp));Box(Modifier.width(40.dp).height(2.dp).background(accent))}
    TopButton("⚙",onThemes)
   }
   Spacer(Modifier.height(62.dp))
   Text(expression.ifBlank{" "},color=Muted,fontSize=18.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.End,maxLines=1)
   Text(display,color=Ink,fontSize=68.sp,fontWeight=FontWeight.Light,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.End,maxLines=1)
   Spacer(Modifier.height(18.dp))
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){listOf("mc","m+","m-","mr").forEach{MemoryKey(it,Modifier.weight(1f),tap)}}
   Spacer(Modifier.height(16.dp))
   RowKeys(listOf("AC","±","%","÷"),accent,tap)
   Spacer(Modifier.height(10.dp));RowKeys(listOf("7","8","9","×"),accent,tap)
   Spacer(Modifier.height(10.dp));RowKeys(listOf("4","5","6","−"),accent,tap)
   Spacer(Modifier.height(10.dp));RowKeys(listOf("1","2","3","+"),accent,tap)
   Spacer(Modifier.height(10.dp))
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){Key("0",false,accent,Modifier.weight(2f).height(72.dp),tap);Key(".",false,accent,Modifier.weight(1f).height(72.dp),tap);Key("=",true,accent,Modifier.weight(1f).height(72.dp),tap)}
   Spacer(Modifier.weight(1f));Text("SIMPLE MATH.  EXTRAORDINARY DESIGN.",color=Muted,fontSize=8.sp,letterSpacing=3.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center)
  }
 }
}
@Composable private fun TopButton(s:String,click:()->Unit){Box(Modifier.size(54.dp).clip(CircleShape).background(Color(0xFF111217)).clickable{click()},contentAlignment=Alignment.Center){Text(s,color=Ink,fontSize=23.sp)}}
@Composable private fun MemoryKey(s:String,m:Modifier,t:(String)->Unit){Box(m.height(56.dp).clip(RoundedCornerShape(28.dp)).background(Card).clickable{t(s)},contentAlignment=Alignment.Center){Text(s,color=Ink,fontSize=17.sp)}}
@Composable private fun RowKeys(keys:List<String>,accent:Color,t:(String)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){keys.forEach{k->Key(k,k in listOf("÷","×","−","+"),accent,Modifier.weight(1f).height(72.dp),t)}}}
@Composable private fun Key(s:String,operator:Boolean,accent:Color,m:Modifier,t:(String)->Unit){val utility=s in listOf("AC","±","%");val bg=if(operator||s=="=")accent else if(utility)Utility else Card;Box(m.clip(RoundedCornerShape(30.dp)).background(bg).clickable{t(s)},contentAlignment=Alignment.Center){Text(s,color=if(operator||s=="=")Space else Ink,fontSize=28.sp,fontWeight=FontWeight.Medium)}}

@Composable private fun HistoryScreen(items:List<Item>,accent:Color,back:()->Unit,use:(Item)->Unit){Column(Modifier.fillMaxSize().padding(22.dp)){Text("‹",color=Ink,fontSize=40.sp,modifier=Modifier.clickable{back()});Text("History",color=Ink,fontSize=28.sp);Spacer(Modifier.height(20.dp));Text("Today",color=Muted);Spacer(Modifier.height(10.dp));if(items.isEmpty())Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("Your calculations will appear here.",color=Muted)}else Column(verticalArrangement=Arrangement.spacedBy(10.dp)){items.take(7).forEach{i->Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card).clickable{use(i)}.padding(18.dp)){Text(i.expression,color=Ink,fontSize=18.sp);Spacer(Modifier.height(8.dp));Text(i.result,color=accent,fontSize=25.sp,fontWeight=FontWeight.SemiBold)}}}}}
@Composable private fun ThemeScreen(selected:Color,select:(Color)->Unit,back:()->Unit){val colors=listOf(Orange,Color(0xFF35A7FF),Color(0xFF20B486),Color(0xFF8A4FFF),Color(0xFFE6E6E6));val names=listOf("Aurora","Ocean","Forest","Purple","Minimal");Column(Modifier.fillMaxSize().padding(22.dp)){Text("‹",color=Ink,fontSize=40.sp,modifier=Modifier.clickable{back()});Text("Custom Themes",color=Ink,fontSize=28.sp);Text("Choose the orbit that feels like you.",color=Muted);Spacer(Modifier.height(24.dp));colors.forEachIndexed{idx,c->Row(Modifier.fillMaxWidth().height(68.dp).padding(vertical=5.dp).clip(RoundedCornerShape(22.dp)).background(Card).clickable{select(c)}.padding(horizontal=18.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(38.dp).clip(CircleShape).background(c));Spacer(Modifier.width(16.dp));Text(names[idx],color=Ink,fontSize=19.sp,modifier=Modifier.weight(1f));if(selected==c)Text("✓",color=c,fontSize=24.sp)}}}}