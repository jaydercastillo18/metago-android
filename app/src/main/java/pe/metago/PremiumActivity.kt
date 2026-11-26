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
 private var dark by mutableStateOf(false)
 private val emerald=Color(0xFF087A55)
 private val petrol=Color(0xFF102D35)
 private val gold=Color(0xFFF4C76B)
 private fun money(n:Long)=String.format(Locale.US,"S/ %,.2f",n/100.0)
 private fun amount(s:String)=runCatching { s.replace(',','.').toBigDecimal().movePointRight(2).longValueExact() }.getOrDefault(0)
 override fun onCreate(savedInstanceState:Bundle?) {
 super.onCreate(savedInstanceState)
 dark=prefs.getBoolean("darkMode",false)
 window.decorView.systemUiVisibility=android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
 Reminder.schedule(this)
 if(android.os.Build.VERSION.SDK_INT>=33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=0) requestPermissions(arrayOf("android.permission.POST_NOTIFICATIONS"),1)
 setContent { MaterialTheme(colorScheme=if(dark)darkColorScheme(primary=Color(0xFF82DBAC),background=Color(0xFF101E20),surface=Color(0xFF203436),onSurface=Color(0xFFF8F6EF)) else lightColorScheme(primary=emerald,background=Color(0xFFF8F6EF),surface=Color.White,onSurface=petrol)){ SideEffect{window.decorView.systemUiVisibility=if(dark)0 else android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR}; Dashboard() } }
 }
 override fun onResume(){super.onResume();Reminder.schedule(this)}
 @Composable private fun Dashboard(){
 var records by remember { mutableStateOf(prefs.getString("entries","[]") ?: "[]") }
 var goal by remember { mutableStateOf(prefs.getLong("goal",200000)) }
 var daily by remember { mutableStateOf(prefs.getLong("daily",1500)) }
 var intro by remember { mutableStateOf(!prefs.getBoolean("premiumIntro",false)) }
 var dialog by remember { mutableStateOf("") }
 var celebration by remember { mutableStateOf("") }
 var profiles by remember { mutableStateOf(prefs.getString("otherGoals","[]") ?: "[]") }
 var otherIndex by remember { mutableIntStateOf(-1) }
 var otherEntry by remember { mutableIntStateOf(-1) }
 var newName by remember { mutableStateOf("") }
 var goalName by remember { mutableStateOf(prefs.getString("goalName","Mi meta personal") ?: "Mi meta personal") }
 var tone by remember { mutableIntStateOf(prefs.getInt("celebrationTone",0)) }
 var effects by remember { mutableStateOf(prefs.getBoolean("effects",true)) }
 var entry by remember { mutableStateOf("") }
 var second by remember { mutableStateOf("") }
 var editIndex by remember { mutableStateOf(-1) }
 var reminder by remember { mutableStateOf(if(prefs.contains("hour")) clock(prefs.getInt("hour",20),prefs.getInt("minute",0)) else "Configurar") }
 val list=remember(records){runCatching{JSONArray(records)}.getOrDefault(JSONArray())}
 val total=(0 until list.length()).sumOf{list.optJSONObject(it)?.optLong("amount")?:0L}
 val remaining=(goal-total).coerceAtLeast(0)
 val progress by animateFloatAsState((total.toFloat()/goal.coerceAtLeast(1)).coerceIn(0f,1f),tween(850),label="progress")
 fun save(){records=list.toString();prefs.edit().putString("entries",records).apply()}
 if(intro){ Column(Modifier.fillMaxSize().background(petrol).systemBarsPadding().padding(32.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Image(painter=androidx.compose.ui.res.painterResource(R.drawable.metago_welcome),contentDescription="Ilustración de crecimiento del ahorro",modifier=Modifier.size(240.dp,180.dp));Text("MetaGO",color=gold,fontSize=48.sp,fontWeight=FontWeight.Bold);Text("Pequeños pasos, grandes sueños.",color=Color.White,fontSize=20.sp);Spacer(Modifier.height(32.dp));Button(onClick={prefs.edit().putBoolean("premiumIntro",true).apply();intro=false}){Text("Comenzar mi aventura")}};return }
 Scaffold(containerColor=MaterialTheme.colorScheme.background){ padding ->
 Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
 Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("🌱 MetaGO",fontSize=30.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurface);Text("Construye tu futuro",color=MaterialTheme.colorScheme.primary)};TextButton(onClick={entry=(goal/100.0).toString();second=(daily/100.0).toString();dialog="settings"}){Text("Ajustes")}}
 Card(colors=CardDefaults.cardColors(containerColor=petrol),shape=RoundedCornerShape(30.dp)){Column(Modifier.fillMaxWidth().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(goalName.uppercase(Locale.ROOT),color=gold,fontWeight=FontWeight.Bold);Spacer(Modifier.height(18.dp));Box(Modifier.size(180.dp),contentAlignment=Alignment.Center){CircularProgressIndicator(progress={progress},modifier=Modifier.fillMaxSize(),color=Color(0xFF34D399),trackColor=Color(0xFF28515A),strokeWidth=12.dp);Column(horizontalAlignment=Alignment.CenterHorizontally){Text("Has ahorrado",color=Color.White);Text(money(total),fontSize=30.sp,fontWeight=FontWeight.Bold,color=Color.White);Text(String.format("%.1f%%",progress*100),color=gold)}};Spacer(Modifier.height(18.dp));Text("Objetivo  "+money(goal),color=Color.White,fontSize=20.sp);Text("Faltan "+money(remaining),color=gold)}}
 Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){Card(Modifier.weight(1f),colors=CardDefaults.cardColors(containerColor=if(dark)Color(0xFF203F35) else Color(0xFFE4F2E8))){Column(Modifier.padding(16.dp)){Text("Aporte diario");Text(money(daily),fontSize=23.sp,fontWeight=FontWeight.Bold)}};Card(Modifier.weight(1f),colors=CardDefaults.cardColors(containerColor=if(dark)Color(0xFF493D26) else Color(0xFFF5E9CE))){Column(Modifier.padding(16.dp)){Text("Días estimados");Text(((remaining+daily-1)/daily.coerceAtLeast(1)).toString(),fontSize=26.sp,fontWeight=FontWeight.Bold)}}}
 Button(onClick={editIndex=-1;entry=(daily/100.0).toString();dialog="deposit"},modifier=Modifier.fillMaxWidth().height(58.dp),shape=RoundedCornerShape(18.dp)){Text("＋ Registrar mi ahorro",fontSize=18.sp)}
 OutlinedButton(onClick={dialog="reminder"},modifier=Modifier.fillMaxWidth()){Text("🔔 Recordatorio · "+reminder)}
 Text("Fecha estimada: "+LocalDate.now().plusDays((((remaining+daily-1)/daily.coerceAtLeast(1))-1).coerceAtLeast(0)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),color=MaterialTheme.colorScheme.primary)
 val today=LocalDate.now()
 val dates=(0 until list.length()).map{list.getJSONObject(it).optString("date")}.toSet()
 val todayAmount=(0 until list.length()).filter{list.getJSONObject(it).optString("date")==today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}.sumOf{list.getJSONObject(it).optLong("amount")}
 Text("Hoy: "+money(todayAmount)+" de "+money(daily),fontWeight=FontWeight.Bold)
 LinearProgressIndicator(progress={(todayAmount.toFloat()/daily.coerceAtLeast(1)).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth())
 Text("Tu semana de ahorro",fontSize=22.sp,fontWeight=FontWeight.Bold)
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){for(i in 6 downTo 0){val day=today.minusDays(i.toLong());val done=dates.contains(day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));Column(horizontalAlignment=Alignment.CenterHorizontally){Text(day.dayOfMonth.toString());Text(if(done)"✓" else "○",color=MaterialTheme.colorScheme.primary,fontSize=24.sp)}}}

 val fmt=DateTimeFormatter.ofPattern("dd/MM/yyyy")
 var streak=0
 var cursor=if(dates.contains(today.format(fmt)))today else today.minusDays(1)
 while(dates.contains(cursor.format(fmt))){streak++;cursor=cursor.minusDays(1)}
 Card(colors=CardDefaults.cardColors(containerColor=if(dark)Color(0xFF493D26) else Color(0xFFF5E9CE)),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Tu racha de ahorro",fontWeight=FontWeight.Bold);Text("🔥 "+streak+" días consecutivos",fontSize=24.sp);Text("Cada día con un aporte cuenta.")}}
 Row(verticalAlignment=Alignment.CenterVertically){Text("Sonidos de celebración",Modifier.weight(1f));Switch(effects,{effects=it;prefs.edit().putBoolean("effects",it).apply()})}
 if(effects){Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("Suave","Clásico","Digital").forEachIndexed{index,name->FilterChip(selected=tone==index,onClick={tone=index;prefs.edit().putInt("celebrationTone",index).apply();playEffect(index)},label={Text(name)})}}}
 Text("Tus recompensas",fontSize=23.sp,fontWeight=FontWeight.Bold)
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){listOf(100,500,1000,2000).forEach{n->Column(horizontalAlignment=Alignment.CenterHorizontally){Text(if(total>=n*100L)"🏆" else "🔒",fontSize=32.sp);Text("S/ "+n,fontSize=12.sp)}}}
 Row(verticalAlignment=Alignment.CenterVertically){Text("Modo oscuro",Modifier.weight(1f));Switch(dark,{dark=it;prefs.edit().putBoolean("darkMode",it).apply()})}
 OutlinedTextField(goalName,{goalName=it.take(60);prefs.edit().putString("goalName",goalName).apply()},label={Text("Nombre de mi meta")},modifier=Modifier.fillMaxWidth(),singleLine=true)
 Text("Mis otras metas",fontSize=23.sp,fontWeight=FontWeight.Bold)
 val others=remember(profiles){runCatching{JSONArray(profiles)}.getOrDefault(JSONArray())}
 OutlinedButton(onClick={newName="";entry="";dialog="newGoal"},modifier=Modifier.fillMaxWidth()){Text("+ Crear otra meta")}
 for(j in 0 until others.length()){val item=others.getJSONObject(j);Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text(item.optString("name"),fontWeight=FontWeight.Bold);Text(money(item.optLong("saved"))+" / "+money(item.optLong("target")));LinearProgressIndicator(progress={(item.optLong("saved").toFloat()/item.optLong("target").coerceAtLeast(1)).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth());TextButton(onClick={otherIndex=j;otherEntry=-1;entry="";dialog="otherDeposit"}){Text("Registrar aporte")};val history=item.optJSONArray("history")?:JSONArray();for(k in history.length()-1 downTo 0){val e=history.getJSONObject(k);TextButton(onClick={otherIndex=j;otherEntry=k;entry=(e.optLong("amount")/100.0).toString();dialog="otherDeposit"}){Text(e.optString("date")+" · "+money(e.optLong("amount")))}}}}}
 Text("Historial de ahorro",fontSize=23.sp,fontWeight=FontWeight.Bold)
 if(list.length()==0)Text("Tu primer depósito empieza una gran historia.")
 for(i in list.length()-1 downTo 0){val o=list.getJSONObject(i);Card(Modifier.fillMaxWidth().clickable{editIndex=i;entry=(o.optLong("amount")/100.0).toString();dialog="deposit"}){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(o.optString("date"),Modifier.weight(1f));Text("+ "+money(o.optLong("amount")),color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold)}}}
 Text("Registro manual · No realiza transferencias",fontSize=12.sp)
 }}
 if(celebration.isNotEmpty()){AlertDialog(onDismissRequest={celebration=""},title={Text("🏆 ¡Nuevo logro!")},text={Column(horizontalAlignment=Alignment.CenterHorizontally){val bounce=remember{Animatable(0.3f)};LaunchedEffect(Unit){bounce.animateTo(1f,spring(dampingRatio=Spring.DampingRatioMediumBouncy))};val scale=bounce.value;Box(Modifier.fillMaxWidth().height(110.dp),contentAlignment=Alignment.Center){Canvas(Modifier.fillMaxSize()){for(i in 0..35){val angle=i*2.39996;val radius=size.minDimension*0.45f*scale;drawCircle(listOf(Color(0xFFF4C76B),Color(0xFF34D399),Color(0xFF087A55))[i%3],radius=3.dp.toPx(),center=androidx.compose.ui.geometry.Offset(size.width/2+kotlin.math.cos(angle).toFloat()*radius*(0.4f+i%5/8f),size.height/2+kotlin.math.sin(angle).toFloat()*radius*(0.4f+i%5/8f)))}};Text("🎉",fontSize=(64*scale).sp)};Text(celebration,fontSize=22.sp,fontWeight=FontWeight.Bold);Text("Tu esfuerzo está dando frutos.")}},confirmButton={TextButton(onClick={celebration=""}){Text("¡Genial!")}})}
 if(dialog=="reminder"){ReminderSettings(onClose={dialog="";reminder=if(prefs.getBoolean("reminderEnabled",true))clock(prefs.getInt("hour",20),prefs.getInt("minute",0)) else "Desactivado"})} else if(dialog=="newGoal"||dialog=="otherDeposit"){AlertDialog(onDismissRequest={dialog=""},title={Text(if(dialog=="newGoal")"Nueva meta" else "Aporte a otra meta")},text={Column{if(dialog=="newGoal")OutlinedTextField(newName,{newName=it.take(60)},label={Text("Nombre")});OutlinedTextField(entry,{entry=it},label={Text("Monto en soles")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))}},confirmButton={TextButton(enabled=amount(entry) in 1..100000000 && (dialog!="newGoal"||newName.isNotBlank()),onClick={val arr=JSONArray(profiles);if(dialog=="newGoal")arr.put(JSONObject().put("name",newName.trim()).put("target",amount(entry)).put("saved",0L)) else {val obj=arr.getJSONObject(otherIndex);val history=obj.optJSONArray("history")?:JSONArray();val old=if(otherEntry>=0)history.getJSONObject(otherEntry).optLong("amount") else 0L;val date=if(otherEntry>=0)history.getJSONObject(otherEntry).optString("date") else LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));val e=JSONObject().put("date",date).put("amount",amount(entry));if(otherEntry>=0)history.put(otherEntry,e) else history.put(e);obj.put("history",history);obj.put("saved",obj.optLong("saved")-old+amount(entry))};profiles=arr.toString();prefs.edit().putString("otherGoals",profiles).apply();dialog=""}){Text("Guardar")}},dismissButton={Row{if(dialog=="otherDeposit"&&otherEntry>=0)TextButton(onClick={val arr=JSONArray(profiles);val obj=arr.getJSONObject(otherIndex);val history=obj.getJSONArray("history");val old=history.getJSONObject(otherEntry).optLong("amount");history.remove(otherEntry);obj.put("saved",obj.optLong("saved")-old);profiles=arr.toString();prefs.edit().putString("otherGoals",profiles).apply();dialog=""}){Text("Eliminar")};TextButton(onClick={dialog=""}){Text("Cancelar")}}})} else if(dialog!=""){AlertDialog(onDismissRequest={dialog=""},title={Text(if(dialog=="settings")"Personaliza tu meta" else "Registrar ahorro")},text={Column{OutlinedTextField(entry,{entry=it},label={Text(if(dialog=="settings")"Meta en soles" else "Monto en soles")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal));if(dialog=="settings")OutlinedTextField(second,{second=it},label={Text("Aporte diario")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))}},confirmButton={TextButton(enabled=amount(entry) in 1..100000000 && (dialog!="settings" || amount(second) in 1..100000000),onClick={if(dialog=="settings"){goal=amount(entry);daily=amount(second);prefs.edit().putLong("goal",goal).putLong("daily",daily).apply()}else{val before=total;val o=JSONObject().put("amount",amount(entry)).put("date",if(editIndex<0)LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) else list.getJSONObject(editIndex).optString("date"));if(editIndex<0)list.put(o) else list.put(editIndex,o);save();if(editIndex<0){val after=before+amount(entry);val unlocked=listOf(10000L,50000L,100000L,200000L,goal).distinct().filter{before<it&&after>=it}.maxOrNull();if(unlocked!=null)celebration="¡Alcanzaste "+money(unlocked)+"!"};if(effects)playEffect(tone)};dialog=""}){Text("Guardar")}},dismissButton={Row{if(dialog=="deposit"&&editIndex>=0)TextButton(onClick={list.remove(editIndex);save();dialog=""}){Text("Eliminar")};TextButton(onClick={dialog=""}){Text("Cancelar")}}})}
 }

 private fun playEffect(index:Int){runCatching{val t=ToneGenerator(AudioManager.STREAM_MUSIC,45);t.startTone(listOf(ToneGenerator.TONE_PROP_ACK,ToneGenerator.TONE_PROP_BEEP,ToneGenerator.TONE_PROP_BEEP2)[index.coerceIn(0,2)],180);android.os.Handler(mainLooper).postDelayed({t.release()},300)}}
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
 Text(clock(h,m),fontSize=36.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.primary)
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
