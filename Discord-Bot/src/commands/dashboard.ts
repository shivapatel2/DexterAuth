import { SlashCommandBuilder, ChatInputCommandInteraction, EmbedBuilder, ApplicationIntegrationType, InteractionContextType } from 'discord.js';
import { db } from '../utils/db';
import { ApiService } from '../services/api';
import { EMOJIS } from '../utils/emojis';

export const dashboardCommand = {
  data: new SlashCommandBuilder()
    .setName('dashboard')
    .setDescription('Fetch and show ALL data for the active app (overview, users, licenses, sessions, team).')
    .setIntegrationTypes(ApplicationIntegrationType.GuildInstall, ApplicationIntegrationType.UserInstall)
    .setContexts(InteractionContextType.Guild, InteractionContextType.BotDM, InteractionContextType.PrivateChannel),

  async execute(interaction: ChatInputCommandInteraction) {
    await interaction.deferReply({ ephemeral: true });
    const secret = db.getUserSecret(interaction.user.id) || undefined;
    const api = new ApiService({ discordId: interaction.user.id, secret });
    try {
      const profile = await api.getDiscordProfile().catch(() => null);
      const appId = (profile as any)?.profile?.selected_app_id;
      if (!appId) return interaction.editReply({ content: `${EMOJIS.ERROR} No app selected. Use \`/app switch\` first.` });

      const [apps, overview, users, licenses, sessions, subs, team, hashes] = await Promise.allSettled([
        api.getApps(),
        api.getOverview(appId),
        api.getUsers(appId),
        api.getLicenses(appId),
        api.getSessions(appId),
        api.getSubscriptions(appId),
        api.getTeamMembers(appId),
        api.getHashes(appId),
      ]);

      const get = (r: any) => r.status === 'fulfilled' ? r.value : null;
      const appsData = get(apps) as any[];
      const ov = get(overview) as any;
      const usersData = get(users) as any[];
      const licensesData = get(licenses) as any[];
      const sessionsData = get(sessions) as any[];
      const subsData = get(subs) as any[];
      const teamData = get(team) as any;
      const hashesData = get(hashes) as any[];

      const activeApp = appsData?.find((a: any) => a.id === appId);
      const embed = new EmbedBuilder()
        .setTitle(`📊 Dashboard — ${activeApp?.name || appId}`)
        .setColor('#5865F2')
        .setDescription(`Live data from \`https://dexterauth.onrender.com\` — all endpoints`)
        .addFields(
          { name: `${EMOJIS.STATS} Overview`, value: `Users: **${ov?.totalUsers ?? 0}** | Licenses: **${ov?.totalLicenses ?? 0}** (used ${ov?.usedLicenses ?? 0}) | Sessions: **${ov?.activeSessions ?? 0}** | Tokens: **${ov?.totalTokens ?? 0}** | Subs: **${ov?.totalSubscriptions ?? 0}** | Banned: **${ov?.bannedUsers ?? 0}**`, inline: false },
          { name: `${EMOJIS.USER} Users (${usersData?.length ?? 0})`, value: usersData?.slice(0,5).map((u:any)=>`\`${u.username}\` — ${u.status} — ${u.subscription_level}`).join('\n') || 'No users', inline: false },
          { name: `${EMOJIS.LICENSE} Licenses (${licensesData?.length ?? 0})`, value: licensesData?.slice(0,5).map((l:any)=>`\`${l.key.slice(0,12)}...\` — ${l.status}`).join('\n') || 'No licenses', inline: false },
          { name: `${EMOJIS.SESSIONS} Sessions (${sessionsData?.length ?? 0})`, value: sessionsData?.slice(0,5).map((s:any)=>`\`${s.user_id.slice(0,8)}\` — ${s.ip || 'N/A'}`).join('\n') || 'No sessions', inline: false },
          { name: `${EMOJIS.PLAN} Subscriptions (${subsData?.length ?? 0})`, value: subsData?.map((s:any)=>`${s.name} (lvl ${s.level})`).join(', ') || 'Default only', inline: false },
          { name: `${EMOJIS.TEAM} Team`, value: JSON.stringify(teamData)?.slice(0, 800) || 'No team', inline: false },
          { name: `${EMOJIS.SHIELD} Hashes (${hashesData?.length ?? 0})`, value: hashesData?.slice(0,3).map((h:any)=>h.hash_value.slice(0,30)).join('\n') || 'None', inline: false },
        )
        .setFooter({ text: `App: ${appId} | Backend: dexterauth.onrender.com | Use /app snapshot for details` })
        .setTimestamp();

      return interaction.editReply({ embeds: [embed] });
    } catch (e:any) {
      return interaction.editReply({ content: `${EMOJIS.ERROR} ${e.message}` });
    }
  }
};
