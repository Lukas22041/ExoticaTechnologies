package exoticatechnologies.commands;

import exoticatechnologies.ETModPlugin;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class et_cleardata implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context)
    {
        if ( context.isInCampaign() )
        {
            ETModPlugin.removeDataFromFleetsAndMarkets();
            ETModPlugin.removeListeners();

            return CommandResult.SUCCESS;
        } else {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

    }
}