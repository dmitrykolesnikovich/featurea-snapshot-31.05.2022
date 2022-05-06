package featurea.modbus.support

import featurea.utils.breakpoint
import featurea.modbus.config.Channel
import featurea.modbus.master.Master

class IndexScope {

    inline operator fun <T> invoke(block: IndexScope.() -> T) = block()

    /*channel*/

    private val channels = mutableListOf<Channel>()
    private val dangerServices = mutableListOf<DangerService>()
    private val journalServices = mutableListOf<JournalService>()
    private val formulaServices = mutableListOf<ChannelFormula>()
    val Channel.dangerService: DangerService get() = dangerServices[index]
    val Channel.journalService: JournalService get() = journalServices[index]
    val Channel.formulaService: ChannelFormula get() = formulaServices[index]

    fun indexChannel(channel: Channel) {
        if (channels.contains(channel)) {
            breakpoint()
        }
        check(!channels.contains(channel))
        channels.add(channel)
        channel.index = channels.size - 1
        dangerServices.add(DangerService(channel))
        journalServices.add(JournalService(channel))
        formulaServices.add(ChannelFormula(channel, this))
    }

    /*master*/

    private val masters = mutableListOf<Master>()
    private val quotaMasterServices = mutableListOf<MasterQuota>()
    val Master.quotaChannels: MutableMap<String, Channel> get() = quotaMasterServices[index].quotaChannels

    fun indexMaster(master: Master) {
        check(!masters.contains(master))
        masters.add(master)
        master.index = masters.size - 1
        quotaMasterServices.add(master.quota)
    }

}
