package blockchain;

class BlockchainSubmission {
    final int magicNumber;
    final String minerId;
    final long timeToGenerate;
    final int zeroes;

    BlockchainSubmission(int magicNumber, String minerId, long timeToGenerate, int zeroes){
        this.magicNumber = magicNumber;
        this.minerId = minerId;
        this.timeToGenerate = timeToGenerate;
        this.zeroes = zeroes;
    }
}
