package com.kamefrede.rpsideas.spells.trick.block;

import com.kamefrede.rpsideas.util.helpers.SpellHelpers;
import net.minecraft.util.math.BlockPos;
import vazkii.psi.api.internal.Vector3;
import vazkii.psi.api.spell.*;
import vazkii.psi.api.spell.param.ParamNumber;
import vazkii.psi.api.spell.param.ParamVector;
import vazkii.psi.api.spell.piece.PieceTrick;


public class PieceTrickTillSequence extends PieceTrick {
    private SpellParam position;
    private SpellParam target;
    private SpellParam maxBlocks;

    public PieceTrickTillSequence(Spell spell) {
        super(spell);
    }

    @Override
    public void initParams() {
        addParam(position = new ParamVector(SpellParam.GENERIC_NAME_POSITION, SpellParam.BLUE, false, false));
        addParam(target = new ParamVector(SpellParam.GENERIC_NAME_TARGET, SpellParam.GREEN, false, false));
        addParam(maxBlocks = new ParamNumber(SpellParam.GENERIC_NAME_MAX, SpellParam.RED, false, true));
    }

    @Override
    public void addToMetadata(SpellMetadata meta) throws SpellCompilationException {
        super.addToMetadata(meta);

        double maxBlocksVal = SpellHelpers.ensurePositiveAndNonzero(this, maxBlocks);

        meta.addStat(EnumSpellStat.POTENCY, (int) (maxBlocksVal * 10));
        meta.addStat(EnumSpellStat.COST, (int) (maxBlocksVal * 10));
    }

    @Override
    public Object execute(SpellContext context) throws SpellRuntimeException {
        Vector3 positionVal = SpellHelpers.getVector3(this, context, position, true, false);
        Vector3 targetVal = SpellHelpers.getVector3(this, context, target, false, false);
        double maxBlocksVal = SpellHelpers.getNumber(this, context, maxBlocks, 0);

        int len = (int) targetVal.mag();
        Vector3 targetNorm = targetVal.copy().normalize();
        for (int i = 0; i < Math.min(len, maxBlocksVal); i++) {
            Vector3 blockVec = positionVal.copy().add(targetNorm.copy().multiply(i));

            BlockPos pos = new BlockPos(blockVec.x, blockVec.y, blockVec.z);
            SpellHelpers.isBlockPosInRadius(context, pos);
            PieceTrickTill.tillBlock(context.caster, context.caster.world, pos);
        }

        return null;

    }
}
