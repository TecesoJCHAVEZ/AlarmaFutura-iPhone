package com.tecesoj.alarmafutura;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity {
    private EditText nameField;
    private TextView selection;
    private LinearLayout alarmsContainer;
    private final Calendar chosen = Calendar.getInstance();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        chosen.add(Calendar.DAY_OF_MONTH, 1);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int spacing = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(spacing, spacing, spacing, spacing);
        scroll.addView(root);

        TextView header = new TextView(this);
        header.setText("Alarma Futura");
        header.setTextSize(26);
        root.addView(header);

        nameField = new EditText(this);
        nameField.setSingleLine(true);
        nameField.setHint("Nombre: cita, viaje, evento...");
        root.addView(nameField);

        selection = new TextView(this);
        selection.setTextSize(16);
        root.addView(selection);
        updateSelection();

        Button date = new Button(this);
        date.setText("Elegir fecha");
        date.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(this,
                    (picker, year, month, day) -> {
                        chosen.set(year, month, day);
                        updateSelection();
                    }, chosen.get(Calendar.YEAR), chosen.get(Calendar.MONTH),
                    chosen.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });
        root.addView(date);

        Button time = new Button(this);
        time.setText("Elegir hora");
        time.setOnClickListener(view -> new TimePickerDialog(this,
                (picker, hour, minute) -> {
                    chosen.set(Calendar.HOUR_OF_DAY, hour);
                    chosen.set(Calendar.MINUTE, minute);
                    chosen.set(Calendar.SECOND, 0);
                    chosen.set(Calendar.MILLISECOND, 0);
                    updateSelection();
                }, chosen.get(Calendar.HOUR_OF_DAY), chosen.get(Calendar.MINUTE), true).show());
        root.addView(time);

        Button create = new Button(this);
        create.setText("Programar alarma");
        create.setOnClickListener(view -> schedule());
        root.addView(create);

        TextView subtitle = new TextView(this);
        subtitle.setText("Alarmas programadas");
        subtitle.setTextSize(20);
        root.addView(subtitle);

        alarmsContainer = new LinearLayout(this);
        alarmsContainer.setOrientation(LinearLayout.VERTICAL);
        root.addView(alarmsContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(scroll);
        refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (alarmsContainer != null) refreshList();
    }

    private void updateSelection() {
        selection.setText("Fecha: " +
                DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
                        .format(new Date(chosen.getTimeInMillis())));
    }

    private void schedule() {
        String name = AlarmRules.normalizedTitle(nameField.getText().toString());
        long fireTime = chosen.getTimeInMillis();
        try {
            AlarmRules.validate(name, fireTime, System.currentTimeMillis());
        } catch (IllegalArgumentException error) {
            toast(error.getMessage());
            return;
        }

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            toast("Autoriza notificaciones y vuelve a pulsar Programar.");
            return;
        }

        if (!AlarmScheduler.canSchedule(this)) {
            if (Build.VERSION.SDK_INT >= 31) {
                Intent settings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + getPackageName()));
                startActivity(settings);
            }
            toast("Autoriza Alarmas y recordatorios y vuelve a programar.");
            return;
        }

        try {
            int id = AlarmRepository.nextId(this);
            AlarmRepository.Entry entry = new AlarmRepository.Entry(id, name, fireTime);
            AlarmScheduler.schedule(this, entry);
            AlarmRepository.add(this, entry);
            nameField.setText("");
            refreshList();
            toast("Alarma programada correctamente.");
            if (Build.VERSION.SDK_INT >= 34) {
                NotificationManager notifications = getSystemService(NotificationManager.class);
                if (!notifications.canUseFullScreenIntent()) {
                    toast("Para pantalla bloqueada, autoriza las alarmas a pantalla completa en Ajustes.");
                }
            }
        } catch (SecurityException error) {
            toast("No se pudo crear: verifica los permisos de alarmas.");
        }
    }

    private void refreshList() {
        alarmsContainer.removeAllViews();
        for (AlarmRepository.Entry entry : AlarmRepository.list(this)) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView label = new TextView(this);
            label.setText(entry.name + "\n" +
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                            .format(new Date(entry.time)));
            row.addView(label, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            Button cancel = new Button(this);
            cancel.setText("Cancelar");
            cancel.setOnClickListener(view -> {
                AlarmScheduler.cancel(this, entry.id);
                AlarmRepository.remove(this, entry.id);
                refreshList();
            });
            row.addView(cancel);
            alarmsContainer.addView(row);
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
