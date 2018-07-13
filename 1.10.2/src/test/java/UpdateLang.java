import com.google.common.base.Throwables;
import com.rwtema.extrautils2.ExtraUtils2;
import com.rwtema.extrautils2.backend.ClientRunnable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class UpdateLang {

	public static void init() {
		ExtraUtils2.proxy.run(new ClientRunnable() {
			@Override
			@SideOnly(Side.CLIENT)
			public void run() {
				File src = new File("C:\\extrautils\\1.10.2\\src\\main\\resources\\assets\\extrautils2\\lang\\");
				File dest = new File("C:\\extrautils\\ExtraUtilities_Localization\\lang\\");

				try {
					FileUtils.copyDirectory(src, dest);
				} catch (IOException e) {
					throw Throwables.propagate(e);
				}
			}
		});

	}
}
