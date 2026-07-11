# ============================================================
# Canopo OBD ProGuard Rules
# ============================================================

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keep @androidx.room.Embedded class *
-keep @androidx.room.Relation class *
-dontwarn androidx.room.paging.**

# --- Compose ---
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# --- Navigation ---
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

# --- Data Models ---
-keep class com.canopobd.data.model.** { *; }
-keep class com.canopobd.data.local.entity.** { *; }

# --- Bluetooth ---
-keep class com.canopobd.bluetooth.** { *; }

# --- Protocol classes ---
-keep class com.canopobd.data.protocol.** { *; }
-keep class com.canopobd.protocol.** { *; }

# --- Enums ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Parcelable ---
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# --- Serializable ---
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# --- R8 full mode ---
-allowaccessmodification
-repackageclasses ''
