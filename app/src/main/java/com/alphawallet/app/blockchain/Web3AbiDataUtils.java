package teleblock.blockchain;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Web3AbiDataUtils {

    /**
     * 交易代币
     */
    public static String encodeTransferData(String toAddress, BigInteger sum) {
        Function function = new Function(
                "transfer",  // function we're calling
                Arrays.asList(new Address(toAddress), new Uint256(sum)),  // Parameters to pass as Solidity Types
                Arrays.asList(new org.web3j.abi.TypeReference<Bool>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 获取账户中的代币数量
     */
    public static String encodeBalanceOfData(String ownerAddress, BigInteger tokenId) {
        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new Address(ownerAddress));
        if (tokenId != null) { // ERC1155
            inputParameters.add(new Uint256(tokenId));
        }
        Function function = new Function(
                "balanceOf",
                inputParameters,
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 获取ERC721代币的元数据
     */
    public static String encodeTokenURIData(BigInteger tokenId) {
        Function function = new Function(
                "tokenURI",
                Arrays.asList(new Uint256(tokenId)),
                Arrays.asList(new org.web3j.abi.TypeReference<Utf8String>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 获取ERC1155代币的元数据
     */
    public static String encodeUriData(BigInteger tokenId) {
        Function function = new Function(
                "uri",
                Arrays.asList(new Uint256(tokenId)),
                Arrays.asList(new org.web3j.abi.TypeReference<Utf8String>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 获取账户拥有的代币
     */
    public static String encodeTokensOfOwnerData(String ownerAddress) {
        Function function = new Function(
                "tokensOfOwner",
                Arrays.asList(new Address(ownerAddress)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     *
     */
    public static String encodeMintData(BigInteger tokenId) {
        Function function = new Function(
                "mint",
                Arrays.asList(new Uint256(tokenId)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 授予将令牌转移tokenId到另一个帐户的权限。令牌转移时，批准将被清除。
     * 一次只能批准一个帐户，因此批准零地址会清除以前的批准。
     */
    public static String encodeApproveData(String toAddress, BigInteger tokenId) {
        Function function = new Function(
                "approve",
                Arrays.asList(new Address(toAddress), new Uint256(tokenId)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 返回批准令牌的帐户
     */
    public static String encodeGetApprovedData(BigInteger tokenId) {
        Function function = new Function(
                "getApproved",
                Arrays.asList(new Uint256(tokenId)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 回收NFT
     */
    public static String encodeRecycleData(String nftAddress, BigInteger tokenId) {
        Function function = new Function(
                "recycle",
                Arrays.asList(new Address(nftAddress), new Uint256(tokenId)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 上架NFT
     */
    public static String encodeListItemData(String nftAddress, BigInteger tokenId, BigInteger price) {
        Function function = new Function(
                "listItem",
                Arrays.asList(new Address(nftAddress), new Uint256(tokenId), new Uint256(price)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 更新NFT的价格
     */
    public static String encodeUpdateListingData(String nftAddress, BigInteger tokenId, BigInteger newPrice) {
        Function function = new Function(
                "updateListing",
                Arrays.asList(new Address(nftAddress), new Uint256(tokenId), new Uint256(newPrice)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 获取NFT的价格
     */
    public static String encodeGetListingData(String nftAddress, BigInteger tokenId) {
        Function function = new Function(
                "getListing",
                Arrays.asList(new Address(nftAddress), new Uint256(tokenId)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 下架NFT
     */
    public static String encodeCancelListingData(String nftAddress, BigInteger tokenId) {
        Function function = new Function(
                "cancelListing",
                Arrays.asList(new Address(nftAddress), new Uint256(tokenId)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }

    /**
     * 购买NFT
     */
    public static String encodeBuyItemData(String nftAddress, BigInteger tokenId) {
        Function function = new Function(
                "buyItem",
                Arrays.asList(new Address(nftAddress), new Uint256(tokenId)),
                Arrays.asList(new org.web3j.abi.TypeReference<Uint>() {
                })
        );
        return FunctionEncoder.encode(function);
    }


    public static String getMintAbiJson() {
        return "[\n" +
                "\t{\n" +
                "\t\t\"inputs\": [],\n" +
                "\t\t\"stateMutability\": \"nonpayable\",\n" +
                "\t\t\"type\": \"constructor\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"anonymous\": false,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": true,\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"account\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": true,\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"operator\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"internalType\": \"bool\",\n" +
                "\t\t\t\t\"name\": \"approved\",\n" +
                "\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"ApprovalForAll\",\n" +
                "\t\t\"type\": \"event\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"anonymous\": false,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": true,\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"operator\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": true,\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"from\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": true,\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"to\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"internalType\": \"uint256[]\",\n" +
                "\t\t\t\t\"name\": \"ids\",\n" +
                "\t\t\t\t\"type\": \"uint256[]\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"internalType\": \"uint256[]\",\n" +
                "\t\t\t\t\"name\": \"values\",\n" +
                "\t\t\t\t\"type\": \"uint256[]\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"TransferBatch\",\n" +
                "\t\t\"type\": \"event\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"anonymous\": false,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": true,\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"operator\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": true,\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"from\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": true,\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"to\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"id\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"value\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"TransferSingle\",\n" +
                "\t\t\"type\": \"event\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"anonymous\": false,\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": false,\n" +
                "\t\t\t\t\"internalType\": \"string\",\n" +
                "\t\t\t\t\"name\": \"value\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"indexed\": true,\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"id\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"URI\",\n" +
                "\t\t\"type\": \"event\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"account\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"id\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"balanceOf\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"address[]\",\n" +
                "\t\t\t\t\"name\": \"accounts\",\n" +
                "\t\t\t\t\"type\": \"address[]\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256[]\",\n" +
                "\t\t\t\t\"name\": \"ids\",\n" +
                "\t\t\t\t\"type\": \"uint256[]\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"balanceOfBatch\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256[]\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"uint256[]\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"account\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"operator\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"isApprovedForAll\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"bool\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"_token_id\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"mint\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"stateMutability\": \"nonpayable\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [],\n" +
                "\t\t\"name\": \"name\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"string\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"from\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"to\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256[]\",\n" +
                "\t\t\t\t\"name\": \"ids\",\n" +
                "\t\t\t\t\"type\": \"uint256[]\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256[]\",\n" +
                "\t\t\t\t\"name\": \"amounts\",\n" +
                "\t\t\t\t\"type\": \"uint256[]\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"bytes\",\n" +
                "\t\t\t\t\"name\": \"data\",\n" +
                "\t\t\t\t\"type\": \"bytes\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"safeBatchTransferFrom\",\n" +
                "\t\t\"outputs\": [],\n" +
                "\t\t\"stateMutability\": \"nonpayable\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"from\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"to\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"id\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"amount\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"bytes\",\n" +
                "\t\t\t\t\"name\": \"data\",\n" +
                "\t\t\t\t\"type\": \"bytes\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"safeTransferFrom\",\n" +
                "\t\t\"outputs\": [],\n" +
                "\t\t\"stateMutability\": \"nonpayable\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"address\",\n" +
                "\t\t\t\t\"name\": \"operator\",\n" +
                "\t\t\t\t\"type\": \"address\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"bool\",\n" +
                "\t\t\t\t\"name\": \"approved\",\n" +
                "\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"setApprovalForAll\",\n" +
                "\t\t\"outputs\": [],\n" +
                "\t\t\"stateMutability\": \"nonpayable\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"_token_id\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"_numbers\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"setTokendIdInfo\",\n" +
                "\t\t\"outputs\": [],\n" +
                "\t\t\"stateMutability\": \"nonpayable\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"bytes4\",\n" +
                "\t\t\t\t\"name\": \"interfaceId\",\n" +
                "\t\t\t\t\"type\": \"bytes4\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"supportsInterface\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"bool\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"bool\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [],\n" +
                "\t\t\"name\": \"symbol\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"string\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"token_ids_mint_num\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"token_ids_num\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"inputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"uint256\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"uint256\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"name\": \"uri\",\n" +
                "\t\t\"outputs\": [\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"internalType\": \"string\",\n" +
                "\t\t\t\t\"name\": \"\",\n" +
                "\t\t\t\t\"type\": \"string\"\n" +
                "\t\t\t}\n" +
                "\t\t],\n" +
                "\t\t\"stateMutability\": \"view\",\n" +
                "\t\t\"type\": \"function\"\n" +
                "\t}\n" +
                "]";
    }
}
