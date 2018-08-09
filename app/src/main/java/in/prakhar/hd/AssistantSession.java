package in.prakhar.hd;

import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class AssistantSession extends VoiceInteractionSession {
    public AssistantSession(Context context) {
        super(context);
    }

    @Override
    public void onHandleAssist(@Nullable Bundle data, @Nullable AssistStructure structure, @Nullable AssistContent content) {
        super.onHandleAssist(data, structure, content);

        Toast.makeText(getContext(), "Assistant working", Toast.LENGTH_SHORT).show();
    }
}
