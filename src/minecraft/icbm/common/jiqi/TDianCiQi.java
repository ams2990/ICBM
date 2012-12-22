package icbm.common.jiqi;

import icbm.common.CommonProxy;
import icbm.common.ZhuYao;
import icbm.common.zhapin.ZhaPin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.electricity.ElectricityNetwork;
import universalelectricity.core.implement.IJouleStorage;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.implement.IRedstoneReceptor;
import universalelectricity.prefab.multiblock.IMultiBlock;
import universalelectricity.prefab.network.IPacketReceiver;
import universalelectricity.prefab.network.PacketManager;
import universalelectricity.prefab.tile.TileEntityElectricityReceiver;

import com.google.common.io.ByteArrayDataInput;

public class TDianCiQi extends TileEntityElectricityReceiver implements IJouleStorage, IPacketReceiver, IMultiBlock, IRedstoneReceptor
{
	// The maximum possible radius for the EMP to strike
	public static final int MAX_RADIUS = 150;

	public float xuanZhuan = 0;
	private float xuanZhuanLu, prevXuanZhuanLu = 0;

	private double dian = 0;

	// The EMP mode. 0 = All, 1 = Missiles Only, 2 = Electricity Only
	public byte muoShi = 0;

	// The EMP explosion radius
	public int banJing = 60;

	private int yongZhe = 0;

	public TDianCiQi()
	{
		super();
		LeiDaJiQiGuanLi.register(this);
	}

	@Override
	public void invalidate()
	{
		LeiDaJiQiGuanLi.unregister(this);
		super.initiate();
	}

	public void updateEntity()
	{
		super.updateEntity();

		if (!this.worldObj.isRemote)
		{
			for (int i = 0; i < 6; i++)
			{
				Vector3 diDian = new Vector3(this);
				diDian.modifyPositionFromSide(ForgeDirection.getOrientation(i));
				TileEntity tileEntity = diDian.getTileEntity(this.worldObj);
				ElectricityNetwork network = ElectricityNetwork.getNetworkFromTileEntity(tileEntity, ForgeDirection.getOrientation(i));

				if (network != null)
				{
					if (!this.isDisabled())
					{
						network.startRequesting(this, (this.getMaxJoules() - this.dian) / this.getVoltage(), this.getVoltage());
						this.setJoules(this.dian + network.consumeElectricity(this).getWatts());
					}
					else
					{
						network.stopRequesting(this);
					}
				}
			}
		}

		if (!this.isDisabled())
		{
			if (this.ticks % 20 == 0 && this.dian > 0)
			{
				this.worldObj.playSoundEffect((int) this.xCoord, (int) this.yCoord, (int) this.zCoord, "icbm.machinehum", 0.5F, (float) (0.85F * this.dian / this.getMaxJoules()));
			}

			this.xuanZhuanLu = (float) (Math.pow(this.dian / this.getMaxJoules(), 2) * 0.5);
			this.xuanZhuan += xuanZhuanLu;
			if (this.xuanZhuan > 360)
				this.xuanZhuan = 0;
		}

		if (!this.worldObj.isRemote)
		{
			if (this.ticks % 3 == 0 && this.yongZhe > 0)
			{
				PacketManager.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 12);
			}

			if (this.ticks % 60 == 0 && this.prevXuanZhuanLu != this.xuanZhuanLu)
			{
				PacketManager.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 35);
			}
		}

		this.prevXuanZhuanLu = this.xuanZhuanLu;
	}

	@Override
	public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
	{
		try
		{
			final int ID = dataStream.readInt();

			if (ID == -1)
			{
				if (dataStream.readBoolean())
				{
					PacketManager.sendPacketToClients(this.getDescriptionPacket(), this.worldObj, new Vector3(this), 15);
					this.yongZhe++;
				}
				else
				{
					this.yongZhe--;
				}
			}
			else if (ID == 1)
			{
				this.dian = dataStream.readDouble();
				this.disabledTicks = dataStream.readInt();
				this.banJing = dataStream.readInt();
				this.muoShi = dataStream.readByte();
			}
			else if (ID == 2)
			{
				this.banJing = dataStream.readInt();
			}
			else if (ID == 3)
			{
				this.muoShi = dataStream.readByte();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		return PacketManager.getPacket(ZhuYao.CHANNEL, this, (int) 1, this.dian, this.disabledTicks, this.banJing, this.muoShi);
	}

	@Override
	public double getVoltage()
	{
		return 220;
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);

		this.dian = par1NBTTagCompound.getDouble("dian");
		this.banJing = par1NBTTagCompound.getInteger("banJing");
		this.muoShi = par1NBTTagCompound.getByte("muoShi");
	}

	/**
	 * Writes a tile entity to NBT.
	 */
	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);

		par1NBTTagCompound.setDouble("dian", this.dian);
		par1NBTTagCompound.setInteger("banJing", this.banJing);
		par1NBTTagCompound.setByte("muoShi", this.muoShi);
	}

	@Override
	public void onPowerOn()
	{
		if (this.dian >= this.getMaxJoules())
		{
			if (this.muoShi == 0 || this.muoShi == 1)
			{
				ZhaPin.dianCiSignal.doBaoZha(worldObj, new Vector3(this.xCoord, this.yCoord, this.zCoord), null, this.banJing, -1);
			}

			if (this.muoShi == 0 || this.muoShi == 2)
			{
				ZhaPin.dianCiWave.doBaoZha(worldObj, new Vector3(this.xCoord, this.yCoord, this.zCoord), null, this.banJing, -1);
			}

			this.dian = 0;
		}
	}

	@Override
	public void onPowerOff()
	{
	}

	@Override
	public void onDestroy(TileEntity callingBlock)
	{
		this.worldObj.setBlockWithNotify(this.xCoord, this.yCoord, this.zCoord, 0);
		this.worldObj.setBlockWithNotify(this.xCoord, this.yCoord + 1, this.zCoord, 0);
	}

	@Override
	public boolean onActivated(EntityPlayer entityPlayer)
	{
		entityPlayer.openGui(ZhuYao.instance, CommonProxy.GUI_EMP_TOWER, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		return true;
	}

	@Override
	public void onCreate(Vector3 position)
	{
		ZhuYao.bJia.makeFakeBlock(this.worldObj, Vector3.add(position, new Vector3(0, 1, 0)), new Vector3(this));
	}

	@Override
	public double getMaxJoules(Object... data)
	{
		return Math.max(25000 * ((float) this.banJing / (float) MAX_RADIUS), 1500000);
	}

	@Override
	public double getJoules(Object... data)
	{
		return this.dian;
	}

	@Override
	public void setJoules(double joules, Object... data)
	{
		this.dian = Math.max(Math.min(joules, this.getMaxJoules()), 0);
	}
}