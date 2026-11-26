package pe.metago
import android.os.Bundle
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class PremiumActivity : ComponentActivity() {
 private val prefs by lazy { getSharedPreferences("meta",0) }
 private val emerald=Color(0xFF087A55)
 private val petrol=Color(0xFF102D35)
 private val gold=Color(0xFFF4C76B)
 private fun money(n:Long)=String.format(Locale.US,"S/ %,.2f",n/100.0)
 private fun amount(s:String)=runCatching { s.replace(',','.').toBigDecimal().movePointRight(2).longValueExact() }.getOrDefault(0)
 override fun onCreate(savedInstanceState:Bundle?) {
 super.onCreate(savedInstanceState)
 window.decorView.systemUiVisibility=android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
 Reminder.schedule(this)
 if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=0) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"),1)
 setContent { MaterialTheme(colorScheme=lightColorScheme(primary=emerald,background=Color(0xFFF8F6EF),surface=Color.White,onSurface=petrol)){ Dashboard() } }
 }
 override fun onResume(){super.onResume();Reminder.schedule(this)}
 @Composable private fun Dashboard(){
 var records by remember { mutableStateOf(prefs.getString("entries","[]") ?: "[]") }
 var goal by remember { mutableStateOf(prefs.getLong("goal",200000)) }
 var daily by remember { mutableStateOf(prefs.getLong("daily",1500)) }
 var intro by remember { mutableStateOf(!prefs.getBoolean("premiumIntro",false)) }
 var dialog by remember { mutableStateOf("") }
 var entry by remember { mutableStateOf("") }
 var second by remember { mutableStateOf("") }
 var editIndex by remember { mutableStateOf(-1) }
 var reminder by remember { mutableStateOf(if(prefs.contains("hour")) clock(prefs.getInt("hour",20),prefs.getInt("minute",0)) else "Configurar") }
 val list=remember(records){runCatching{JSONArray(records)}.getOrDefault(JSONArray())}
 val total=(0 until list.length()).sumOf{list.optJSONObject(it)?.optLong("amount")?:0L}
 val remaining=(goal-total).coerceAtLeast(0)
 val progress by animateFloatAsState((total.toFloat()/goal.coerceAtLeast(1)).coerceIn(0f,1f),tween(850),label="progress")
 fun save(){records=list.toString();prefs.edit().putString("entries",records).apply()}
 if(intro){ Column(Modifier.fillMaxSize().background(petrol).systemBarsPadding().padding(32.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text("🌱",fontSize=100.sp);Text("MetaGO",color=gold,fontSize=48.sp,fontWeight=FontWeight.Bold);Text("Pequeños pasos, grandes sueños.",color=Color.White,fontSize=20.sp);Spacer(Modifier.height(32.dp));Button(onClick={prefs.edit().putBoolean("premiumIntro",true).apply();intro=false}){Text("Comenzar mi aventura")}};return }
 Scaffold(containerColor=Color(0xFFF8F6EF)){ padding ->
 Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
 Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("🌱 MetaGO",fontSize=30.sp,fontWeight=FontWeight.Bold,color=petrol);Text("Construye tu futuro",color=emerald)};TextButton(onClick={entry=(goal/100.0).toString();second=(daily/100.0).toString();dialog="settings"}){Text("Ajustes")}}
 Card(colors=CardDefaults.cardColors(containerColor=petrol),shape=RoundedCornerShape(30.dp)){Column(Modifier.fillMaxWidth().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("MI META PERSONAL",color=gold,fontWeight=FontWeight.Bold);Spacer(Modifier.height(18.dp));Box(Modifier.size(180.dp),contentAlignment=Alignment.Center){CircularProgressIndicator(progress={progress},modifier=Modifier.fillMaxSize(),color=Color(0xFF34D399),trackColor=Color(0xFF28515A),strokeWidth=12.dp);Column(horizontalAlignment=Alignment.CenterHorizontally){Text("Has ahorrado",color=Color.White);Text(money(total),fontSize=30.sp,fontWeight=FontWeight.Bold,color=Color.White);Text(String.format("%.1f%%",progress*100),color=gold)}};Spacer(Modifier.height(18.dp));Text("Objetivo  "+money(goal),color=Color.White,fontSize=20.sp);Text("Faltan "+money(remaining),color=gold)}}
 Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Card(Modifier.weight(1f),colors=CardDefaults.cardColors(containerColor=Color(0xFFE4F2E8))){Column(Modifier.padding(16.dp)){Text("Aporte diario");Text(money(daily),fontSize=23.sp,fontWeight=FontWeight.Bold)}};Card(Modifier.weight(1f),colors=CardDefaults.cardColors(containerColor=Color(0xFFF5E9CE))){Column(Modifier.padding(16.dp)){Text("Días estimados");Text(((remaining+daily-1)/daily.coerceAtLeast(1)).toString(),fontSize=26.sp,fontWeight=FontWeight.Bold)}}}
 Button(onClick={editIndex=-1;entry=(daily/100.0).toString();dialog="deposit"},modifier=Modifier.fillMaxWidth().height(58.dp),shape=RoundedCornerShape(18.dp)){Text("＋ Registrar mi ahorro",fontSize=18.sp)}
 OutlinedButton(onClick={dialog="reminder"},modifier=Modifier.fillMaxWidth()){Text("🔔 Recordatorio · "+reminder)}
 Text("Fecha estimada: "+LocalDate.now().plusDays((((remaining+daily-1)/daily.coerceAtLeast(1))-1).coerceAtLeast(0)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),color=emerald)
 val today=LocalDate.now()
 val dates=(0 until list.length()).map{list.getJSONObject(it).optString("date")}.toSet()
 val todayAmount=(0 until list.length()).filter{list.getJSONObject(it).optString("date")==today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}.sumOf{list.getJSONObject(it).optLong("amount")}
 Text("Hoy: "+money(todayAmount)+" de "+money(daily),fontWeight=FontWeight.Bold)
 LinearProgressIndicator(progress={(todayAmount.toFloat()/daily.coerceAtLeast(1)).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth())
 Text("Tu semana de ahorro",fontSize=22.sp,fontWeight=FontWeight.Bold)
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){for(i in 6 downTo 0){val day=today.minusDays(i.toLong());val done=dates.contains(day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));Column(horizontalAlignment=Alignment.CenterHorizontally){Text(day.dayOfMonth.toString());Text(if(done)"✓" else "○",color=emerald,fontSize=24.sp)}}}

 Text("Tus recompensas",fontSize=23.sp,fontWeight=FontWeight.Bold)
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){listOf(100,500,1000,2000).forEach{n->Column(horizontalAlignment=Alignment.CenterHorizontally){Text(if(total>=n*100L)"🏆" else "🔒",fontSize=32.sp);Text("S/ "+n,fontSize=12.sp)}}}
 Text("Historial de ahorro",fontSize=23.sp,fontWeight=FontWeight.Bold)
 if(list.length()==0)Text("Tu primer depósito empieza una gran historia.")
 for(i in list.length()-1 downTo 0){val o=list.getJSONObject(i);Card(Modifier.fillMaxWidth().clickable{editIndex=i;entry=(o.optLong("amount")/100.0).toString();dialog="deposit"}){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(o.optString("date"),Modifier.weight(1f));Text("+ "+money(o.optLong("amount")),color=emerald,fontWeight=FontWeight.Bold)}}}
 Text("Registro manual · No realiza transferencias",fontSize=12.sp)
 }}
 if(dialog=="reminder"){ReminderSettings(onClose={dialog="";reminder=if(prefs.getBoolean("reminderEnabled",true))clock(prefs.getInt("hour",20),prefs.getInt("minute",0)) else "Desactivado"})} else if(dialog!=""){AlertDialog(onDismissRequest={dialog=""},title={Text(if(dialog=="settings")"Personaliza tu meta" else "Registrar ahorro")},text={Column{OutlinedTextField(entry,{entry=it},label={Text(if(dialog=="settings")"Meta en soles" else "Monto en soles")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal));if(dialog=="settings")OutlinedTextField(second,{second=it},label={Text("Aporte diario")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))}},confirmButton={TextButton(enabled=amount(entry) in 1..100000000 && (dialog!="settings" || amount(second) in 1..100000000),onClick={if(dialog=="settings"){goal=amount(entry);daily=amount(second);prefs.edit().putLong("goal",goal).putLong("daily",daily).apply()}else{val o=JSONObject().put("amount",amount(entry)).put("date",if(editIndex<0)LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) else list.getJSONObject(editIndex).optString("date"));if(editIndex<0)list.put(o) else list.put(editIndex,o);save();runCatching{val t=ToneGenerator(AudioManager.STREAM_MUSIC,45);t.startTone(ToneGenerator.TONE_PROP_ACK,180);android.os.Handler(mainLooper).postDelayed({t.release()},300)}};dialog=""}){Text("Guardar")}},dismissButton={Row{if(dialog=="deposit"&&editIndex>=0)TextButton(onClick={list.remove(editIndex);save();dialog=""}){Text("Eliminar")};TextButton(onClick={dialog=""}){Text("Cancelar")}}})}
 }

 private fun clock(h:Int,m:Int)=String.format(Locale.US,"%d:%02d %s",if(h%12==0)12 else h%12,m,if(h<12)"AM" else "PM")
 @Composable private fun ReminderSettings(onClose:()->Unit){
 var h by remember{mutableIntStateOf(prefs.getInt("hour",20))}
 var m by remember{mutableIntStateOf(prefs.getInt("minute",0))}
 var enabled by remember{mutableStateOf(prefs.getBoolean("reminderEnabled",true))}
 var sound by remember{mutableStateOf(prefs.getBoolean("reminderSound",true))}
 var vibration by remember{mutableStateOf(prefs.getBoolean("reminderVibration",true))}
 fun save(){prefs.edit().putInt("hour",h).putInt("minute",m).putBoolean("reminderEnabled",enabled).putBoolean("reminderSound",sound).putBoolean("reminderVibration",vibration).apply();Reminder.schedule(this)}
 AlertDialog(onDismissRequest=onClose,title={Text("Mi recordatorio diario")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)){
 Text("Una vez al día, hasta completar tu meta.")
 Text(clock(h,m),fontSize=36.sp,fontWeight=FontWeight.Bold,color=emerald)
 Text("AM: medianoche a mediodía · PM: mediodía a medianoche")
 OutlinedButton(onClick={TimePickerDialog(this@PremiumActivity,{_,hh,mm->h=hh;m=mm},h,m,false).show()}){Text("Cambiar hora")}
 Row(verticalAlignment=Alignment.CenterVertically){Text("Activar recordatorio",Modifier.weight(1f));Switch(enabled,{enabled=it})}
 Row(verticalAlignment=Alignment.CenterVertically){Text("Sonido",Modifier.weight(1f));Switch(sound,{sound=it})}
 Row(verticalAlignment=Alignment.CenterVertically){Text("Vibración",Modifier.weight(1f));Switch(vibration,{vibration=it})}
 val next=java.time.LocalDateTime.now().let{now->val t=now.toLocalDate().atTime(h,m);if(t.isAfter(now))t else t.plusDays(1)}
 if(enabled)Text("Próximo: "+next.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))+" · "+clock(h,m))
 if(!Reminder.exact(this@PremiumActivity)){Text("Android puede retrasar la notificación. Activa alarmas exactas.");TextButton(onClick={save();if(android.os.Build.VERSION.SDK_INT>=31)runCatching{startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+packageName)))}}){Text("Permitir alarmas exactas")}}
 TextButton(onClick={if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=0)requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"),1) else {save();Reminder.test(this@PremiumActivity);Toast.makeText(this@PremiumActivity,"Revisa tu notificación de prueba",Toast.LENGTH_SHORT).show()}}){Text("Probar notificación ahora")}
 Text("Si ya completaste tu aporte diario, no te molestaremos.",fontSize=12.sp)
 }},confirmButton={TextButton(onClick={save();onClose()}){Text("Guardar")}},dismissButton={TextButton(onClick=onClose){Text("Cerrar")}})
 }
}
