package blockchain;

class BlockchainSubmission {
    final int magicNumber;
    final int minerId;
    final long timeToGenerate;
    final int zeroes;

    BlockchainSubmission(int magicNumber, int minerId, long timeToGenerate, int zeroes){
        this.magicNumber = magicNumber;
        this.minerId = minerId;
        this.timeToGenerate = timeToGenerate;
        this.zeroes = zeroes;
    }
}
