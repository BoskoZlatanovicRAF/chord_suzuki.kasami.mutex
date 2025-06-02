package cli.command;

import app.AppConfig;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.LeaveNodeMessage;
import servent.message.util.MessageUtil;

public class LeaveCommand implements CLICommand{

    private CLIParser parser;
    private SimpleServentListener listener;

    public LeaveCommand(CLIParser parser, SimpleServentListener listener) {
        this.parser = parser;
        this.listener = listener;
    }
    @Override
    public String commandName() {
        return "leave";
    }

    @Override
    public void execute(String args) {
        AppConfig.timestampedStandardPrint("Gracefully leaving the system...");

        // KORAK 1: Obavesti bootstrap da napuštamo sistem
        AppConfig.chordState.notifyBootstrapLeaving();

        // KORAK 2: Ako nismo sami u sistemu, pošalji svoje podatke sledbeniku
        if (AppConfig.chordState.getSuccessorTable()[0] != null &&
                AppConfig.chordState.getSuccessorTable()[0].getListenerPort() != AppConfig.myServentInfo.getListenerPort()) {


            AppConfig.timestampedErrorPrint("[LeaveCommand] Sending leave message to successor: " +AppConfig.chordState.getSuccessorTable()[0]);
            LeaveNodeMessage leaveMsg = new LeaveNodeMessage(
                    AppConfig.myServentInfo.getListenerPort(),
                    AppConfig.chordState.getSuccessorTable()[0].getListenerPort(),
                    AppConfig.chordState.getValueMap()
            );

            MessageUtil.sendMessage(leaveMsg);

            // Kratka pauza da se poruka pošalje
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        AppConfig.timestampedStandardPrint("Left the system gracefully");
        parser.stop();
        listener.stop();
        AppConfig.chordState.getHealthCheckThread().stop();
    }
}
