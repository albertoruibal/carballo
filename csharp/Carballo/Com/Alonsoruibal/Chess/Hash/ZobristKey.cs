/*
 * Code converted with Sharpen
 * 
 */using Com.Alonsoruibal.Chess;
using Com.Alonsoruibal.Chess.Bitboard;
using Com.Alonsoruibal.Chess.Hash;
using Sharpen;

namespace Com.Alonsoruibal.Chess.Hash
{
	public class ZobristKey
	{
		public const long exclusionKey = unchecked((long)(0x5472a27925a2a2f5L));

		public static readonly long[][] pawn = new long[][] { new long[] { unchecked((long
			)(0x79ad695501e7d1e8L)), unchecked((long)(0x8249a47aee0e41f7L)), unchecked((long
			)(0x637a7780decfc0d9L)), unchecked((long)(0x19fc8a768cf4b6d4L)), unchecked((long
			)(0x7bcbc38da25a7f3cL)), unchecked((long)(0x5093417aa8a7ed5eL)), unchecked((long
			)(0x7fb9f855a997142L)), unchecked((long)(0x5355f900c2a82dc7L)), unchecked((long)
			(0xe99d662af4243939L)), unchecked((long)(0xa49cd132bfbf7cc4L)), unchecked((long)
			(0xce26c0b95c980d9L)), unchecked((long)(0xbb6e2924f03912eaL)), unchecked((long)(
			0x24c3c94df9c8d3f6L)), unchecked((long)(0xdabf2ac8201752fcL)), unchecked((long)(
			0xf145b6beccdea195L)), unchecked((long)(0x14acbaf4777d5776L)), unchecked((long)(
			0xf9b89d3e99a075c2L)), unchecked((long)(0x70ac4cd9f04f21f5L)), unchecked((long)(
			0x9a85ac909a24eaa1L)), unchecked((long)(0xee954d3c7b411f47L)), unchecked((long)(
			0x72b12c32127fed2bL)), unchecked((long)(0x54b3f4fa5f40d873L)), unchecked((long)(
			0x8535f040b9744ff1L)), unchecked((long)(0x27e6ad7891165c3fL)), unchecked((long)(
			0x8de8dca9f03cc54eL)), unchecked((long)(0xff07f64ef8ed14d0L)), unchecked((long)(
			0x92237ac237f3859L)), unchecked((long)(0x87bf02c6b49e2ae9L)), unchecked((long)(0x1920c04d47267bbdL
			)), unchecked((long)(0xae4a9346cc3f7cf2L)), unchecked((long)(0xa366e5b8c54f48b8L
			)), unchecked((long)(0x87b3e2b2b5c907b1L)), unchecked((long)(0x6304d09a0b3738c4L
			)), unchecked((long)(0x4f80f7a035dafb04L)), unchecked((long)(0x9a74acb964e78cb3L
			)), unchecked((long)(0x1e1032911fa78984L)), unchecked((long)(0x5bfea5b4712768e9L
			)), unchecked((long)(0x390e5fb44d01144bL)), unchecked((long)(0xb3f22c3d0b0b38edL
			)), unchecked((long)(0x9c1633264db49c89L)), unchecked((long)(0x7b32f7d1e03680ecL
			)), unchecked((long)(0xef927dbcf00c20f2L)), unchecked((long)(0xdfd395339cdbf4a7L
			)), unchecked((long)(0x6503080440750644L)), unchecked((long)(0x1881afc9a3a701d6L
			)), unchecked((long)(0x506aacf489889342L)), unchecked((long)(0x5b9b63eb9ceff80cL
			)), unchecked((long)(0x2171e64683023a08L)), unchecked((long)(0xede6c87f8477609dL
			)), unchecked((long)(0x3c79a0ff5580ef7fL)), unchecked((long)(0xf538639ce705b824L
			)), unchecked((long)(0xcf464cec899a2f8aL)), unchecked((long)(0x4a750a09ce9573f7L
			)), unchecked((long)(0xb5889c6e15630a75L)), unchecked((long)(0x5a7e8a57db91b77L)
			), unchecked((long)(0xb9fd7620e7316243L)), unchecked((long)(0x73a1921916591cbdL)
			), unchecked((long)(0x70eb093b15b290ccL)), unchecked((long)(0x920e449535dd359eL)
			), unchecked((long)(0x43fcae60cc0eba0L)), unchecked((long)(0xa246637cff328532L))
			, unchecked((long)(0x97d7374c60087b73L)), unchecked((long)(0x86536b8cf3428a8cL))
			, unchecked((long)(0x799e81f05bc93f31L)) }, new long[] { unchecked((long)(0xe83a908ff2fb60caL
			)), unchecked((long)(0xfbbad1f61042279L)), unchecked((long)(0x3290ac3a203001bfL)
			), unchecked((long)(0x75834465489c0c89L)), unchecked((long)(0x9c15f73e62a76ae2L)
			), unchecked((long)(0x44db015024623547L)), unchecked((long)(0x2af7398005aaa5c7L)
			), unchecked((long)(0x9d39247e33776d41L)), unchecked((long)(0x239f8b2d7ff719ccL)
			), unchecked((long)(0x5db4832046f3d9e5L)), unchecked((long)(0x11355146fd56395L))
			, unchecked((long)(0x40bdf15d4a672e32L)), unchecked((long)(0xd021ff5cd13a2ed5L))
			, unchecked((long)(0x9605d5f0e25ec3b0L)), unchecked((long)(0x1a083822ceafe02dL))
			, unchecked((long)(0xd7e765d58755c10L)), unchecked((long)(0x4bb38de5e7219443L)), 
			unchecked((long)(0x331478f3af51bbe6L)), unchecked((long)(0xf3218f1c9510786cL)), 
			unchecked((long)(0x82c7709e781eb7ccL)), unchecked((long)(0x7d11cdb1c3b7adf0L)), 
			unchecked((long)(0x7449bbff801fed0bL)), unchecked((long)(0x679f848f6e8fc971L)), 
			unchecked((long)(0x5d1a1ae85b49aa1L)), unchecked((long)(0x24aa6c514da27500L)), unchecked(
			(long)(0xc9452ca81a09d85dL)), unchecked((long)(0x7b0500ac42047ac4L)), unchecked(
			(long)(0xb4ab30f062b19abfL)), unchecked((long)(0x19f3c751d3e92ae1L)), unchecked(
			(long)(0x87d2074b81d79217L)), unchecked((long)(0x8dbd98a352afd40bL)), unchecked(
			(long)(0xaa649c6ebcfd50fcL)), unchecked((long)(0x735e2b97a4c45a23L)), unchecked(
			(long)(0x3575668334a1dd3bL)), unchecked((long)(0x9d1bc9a3dd90a94L)), unchecked((
			long)(0x637b2b34ff93c040L)), unchecked((long)(0x3488b95b0f1850fL)), unchecked((long
			)(0xa71b9b83461cbd93L)), unchecked((long)(0x14a68fd73c910841L)), unchecked((long
			)(0x4c9f34427501b447L)), unchecked((long)(0xfcf7fe8a3430b241L)), unchecked((long
			)(0x5c82c505db9ab0faL)), unchecked((long)(0x51ebdc4ab9ba3035L)), unchecked((long
			)(0x9f74d14f7454a824L)), unchecked((long)(0xbf983fe0fe5d8244L)), unchecked((long
			)(0xd310a7c2ce9b6555L)), unchecked((long)(0x1fcbacd259bf02e7L)), unchecked((long
			)(0x18727070f1bd400bL)), unchecked((long)(0x96d693460cc37e5dL)), unchecked((long
			)(0x4de0b0f40f32a7b8L)), unchecked((long)(0x6568fca92c76a243L)), unchecked((long
			)(0x11d505d4c351bd7fL)), unchecked((long)(0x7ef48f2b83024e20L)), unchecked((long
			)(0xb9bc6c87167c33e7L)), unchecked((long)(0x8c74c368081b3075L)), unchecked((long
			)(0x3253a729b9ba3ddeL)), unchecked((long)(0xec16ca8aea98ad76L)), unchecked((long
			)(0x63dc359d8d231b78L)), unchecked((long)(0x93c5b5f47356388bL)), unchecked((long
			)(0x39f890f579f92f88L)), unchecked((long)(0x5f0f4a5898171bb6L)), unchecked((long
			)(0x42880b0236e4d951L)), unchecked((long)(0x6d2bdcdae2919661L)), unchecked((long
			)(0x42e240cb63689f2fL)) } };

		public static readonly long[][] rook = new long[][] { new long[] { unchecked((long
			)(0xd18d8549d140caeaL)), unchecked((long)(0x1cfc8bed0d681639L)), unchecked((long
			)(0xca1e3785a9e724e5L)), unchecked((long)(0xb67c1fa481680af8L)), unchecked((long
			)(0xdfea21ea9e7557e3L)), unchecked((long)(0xd6b6d0ecc617c699L)), unchecked((long
			)(0xfa7e393983325753L)), unchecked((long)(0xa09e8c8c35ab96deL)), unchecked((long
			)(0x7983eed3740847d5L)), unchecked((long)(0x298af231c85bafabL)), unchecked((long
			)(0x2680b122baa28d97L)), unchecked((long)(0x734de8181f6ec39aL)), unchecked((long
			)(0x53898e4c3910da55L)), unchecked((long)(0x1761f93a44d5aefeL)), unchecked((long
			)(0xe4dbf0634473f5d2L)), unchecked((long)(0x4ed0fe7e9dc91335L)), unchecked((long
			)(0x261e4e4c0a333a9dL)), unchecked((long)(0x219b97e26ffc81bdL)), unchecked((long
			)(0x66b4835d9eafea22L)), unchecked((long)(0x4cc317fb9cddd023L)), unchecked((long
			)(0x50b704cab602c329L)), unchecked((long)(0xedb454e7badc0805L)), unchecked((long
			)(0x9e17e49642a3e4c1L)), unchecked((long)(0x66c1a2a1a60cd889L)), unchecked((long
			)(0x36f60e2ba4fa6800L)), unchecked((long)(0x38b6525c21a42b0eL)), unchecked((long
			)(0xf4f5d05c10cab243L)), unchecked((long)(0xcf3f4688801eb9aaL)), unchecked((long
			)(0x1ddc0325259b27deL)), unchecked((long)(0xb9571fa04dc089c8L)), unchecked((long
			)(0xd7504dfa8816edbbL)), unchecked((long)(0x1fe2cca76517db90L)), unchecked((long
			)(0xe699ed85b0dfb40dL)), unchecked((long)(0xd4347f66ec8941c3L)), unchecked((long
			)(0xf4d14597e660f855L)), unchecked((long)(0x8b889d624d44885dL)), unchecked((long
			)(0x258e5a80c7204c4bL)), unchecked((long)(0xaf0c317d32adaa8aL)), unchecked((long
			)(0x9c4cd6257c5a3603L)), unchecked((long)(0xeb3593803173e0ceL)), unchecked((long
			)(0xb090a7560a968e3L)), unchecked((long)(0x2cf9c8ca052f6e9fL)), unchecked((long)
			(0x116d0016cb948f09L)), unchecked((long)(0xa59e0bd101731a28L)), unchecked((long)
			(0x63767572ae3d6174L)), unchecked((long)(0xab4f6451cc1d45ecL)), unchecked((long)
			(0xc2a1e7b5b459aeb5L)), unchecked((long)(0x2472f6207c2d0484L)), unchecked((long)
			(0x804456af10f5fb53L)), unchecked((long)(0xd74bbe77e6116ac7L)), unchecked((long)
			(0x7c0828dd624ec390L)), unchecked((long)(0x14a195640116f336L)), unchecked((long)
			(0x2eab8ca63ce802d7L)), unchecked((long)(0xc6e57a78fbd986e0L)), unchecked((long)
			(0x58efc10b06a2068dL)), unchecked((long)(0xabeeddb2dde06ff1L)), unchecked((long)
			(0x12a8f216af9418c2L)), unchecked((long)(0xd4490ad526f14431L)), unchecked((long)
			(0xb49c3b3995091a36L)), unchecked((long)(0x5b45e522e4b1b4efL)), unchecked((long)
			(0xa1e9300cd8520548L)), unchecked((long)(0x49787fef17af9924L)), unchecked((long)
			(0x3219a39ee587a30L)), unchecked((long)(0xebe9ea2adf4321c7L)) }, new long[] { unchecked(
			(long)(0x10dcd78e3851a492L)), unchecked((long)(0xb438c2b67f98e5e9L)), unchecked(
			(long)(0x43954b3252dc25e5L)), unchecked((long)(0xab9090168dd05f34L)), unchecked(
			(long)(0xce68341f79893389L)), unchecked((long)(0x36833336d068f707L)), unchecked(
			(long)(0xdcdd7d20903d0c25L)), unchecked((long)(0xda3a361b1c5157b1L)), unchecked(
			(long)(0xaf08da9177dda93dL)), unchecked((long)(0xac12fb171817eee7L)), unchecked(
			(long)(0x1fff7ac80904bf45L)), unchecked((long)(0xa9119b60369ffebdL)), unchecked(
			(long)(0xbfced1b0048eac50L)), unchecked((long)(0xb67b7896167b4c84L)), unchecked(
			(long)(0x9b3cdb65f82ca382L)), unchecked((long)(0xdbc27ab5447822bfL)), unchecked(
			(long)(0x6dd856d94d259236L)), unchecked((long)(0x67378d8eccef96cbL)), unchecked(
			(long)(0x9fc477de4ed681daL)), unchecked((long)(0xf3b8b6675a6507ffL)), unchecked(
			(long)(0xc3a9dc228caac9e9L)), unchecked((long)(0xc37b45b3f8d6f2baL)), unchecked(
			(long)(0xb559eb1d04e5e932L)), unchecked((long)(0x1b0cab936e65c744L)), unchecked(
			(long)(0x7440fb816508c4feL)), unchecked((long)(0x9d266d6a1cc0542cL)), unchecked(
			(long)(0x4dda48153c94938aL)), unchecked((long)(0x74c04bf1790c0efeL)), unchecked(
			(long)(0xe1925c71285279f5L)), unchecked((long)(0x8a8e849eb32781a5L)), unchecked(
			(long)(0x73973751f12dd5eL)), unchecked((long)(0xa319ce15b0b4db31L)), unchecked((
			long)(0x94ebc8abcfb56daeL)), unchecked((long)(0xd7a023a73260b45cL)), unchecked((
			long)(0x72c8834a5957b511L)), unchecked((long)(0x8f8419a348f296bfL)), unchecked((
			long)(0x1e152328f3318deaL)), unchecked((long)(0x4838d65f6ef6748fL)), unchecked((
			long)(0xd6bf7baee43cac40L)), unchecked((long)(0x13328503df48229fL)), unchecked((
			long)(0xdd69a0d8ab3b546dL)), unchecked((long)(0x65ca5b96b7552210L)), unchecked((
			long)(0x2fd7e4b9e72cd38cL)), unchecked((long)(0x51d2b1ab2ddfb636L)), unchecked((
			long)(0x9d1d84fcce371425L)), unchecked((long)(0xa44cfe79ae538bbeL)), unchecked((
			long)(0xde68a2355b93cae6L)), unchecked((long)(0x9fc10d0f989993e0L)), unchecked((
			long)(0x3a938fee32d29981L)), unchecked((long)(0x2c5e9deb57ef4743L)), unchecked((
			long)(0x1e99b96e70a9be8bL)), unchecked((long)(0x764dbeae7fa4f3a6L)), unchecked((
			long)(0xaac40a2703d9bea0L)), unchecked((long)(0x1a8c1e992b941148L)), unchecked((
			long)(0x73aa8a564fb7ac9eL)), unchecked((long)(0x604d51b25fbf70e2L)), unchecked((
			long)(0x8fe88b57305e2ab6L)), unchecked((long)(0x89039d79d6fc5c5cL)), unchecked((
			long)(0x9bfb227ebdf4c5ceL)), unchecked((long)(0x7f7cc39420a3a545L)), unchecked((
			long)(0x3f6c6af859d80055L)), unchecked((long)(0xc8763c5b08d1908cL)), unchecked((
			long)(0x469356c504ec9f9dL)), unchecked((long)(0x26e6db8ffdf5adfeL)) } };

		public static readonly long[][] knight = new long[][] { new long[] { unchecked((long
			)(0x3bba57b68871b59dL)), unchecked((long)(0xdf1d9f9d784ba010L)), unchecked((long
			)(0x94061b871e04df75L)), unchecked((long)(0x9315e5eb3a129aceL)), unchecked((long
			)(0x8bd35cc38336615L)), unchecked((long)(0xfe9a44e9362f05faL)), unchecked((long)
			(0x78e37644e7cad29eL)), unchecked((long)(0xc547f57e42a7444eL)), unchecked((long)
			(0x4f2a5cb07f6a35b3L)), unchecked((long)(0xa2f61bb6e437fdb5L)), unchecked((long)
			(0xa74049dac312ac71L)), unchecked((long)(0x336f52f8ff4728e7L)), unchecked((long)
			(0xd95be88cd210ffa7L)), unchecked((long)(0xd7f4f2448c0ceb81L)), unchecked((long)
			(0xf7a255d83bc373f8L)), unchecked((long)(0xd2b7adeeded1f73fL)), unchecked((long)
			(0x4c0563b89f495ac3L)), unchecked((long)(0x18fcf680573fa594L)), unchecked((long)
			(0xfcaf55c1bf8a4424L)), unchecked((long)(0x39b0bf7dde437ba2L)), unchecked((long)
			(0xf3a678cad9a2e38cL)), unchecked((long)(0x7ba2484c8a0fd54eL)), unchecked((long)
			(0x16b9f7e06c453a21L)), unchecked((long)(0x87d380bda5bf7859L)), unchecked((long)
			(0x35cab62109dd038aL)), unchecked((long)(0x32095b6d4ab5f9b1L)), unchecked((long)
			(0x3810e399b6f65ba2L)), unchecked((long)(0x9d1d60e5076f5b6fL)), unchecked((long)
			(0x7a1ee967d27579e2L)), unchecked((long)(0x68ca39053261169fL)), unchecked((long)
			(0x8cffa9412eb642c1L)), unchecked((long)(0x40e087931a00930dL)), unchecked((long)
			(0x9d1dfa2efc557f73L)), unchecked((long)(0x52ab92beb9613989L)), unchecked((long)
			(0x528f7c8602c5807bL)), unchecked((long)(0xd941aca44b20a45bL)), unchecked((long)
			(0x4361c0ca3f692f12L)), unchecked((long)(0x513e5e634c70e331L)), unchecked((long)
			(0x77a225a07cc2c6bdL)), unchecked((long)(0xa90b24499fcfafb1L)), unchecked((long)
			(0x284c847b9d887aaeL)), unchecked((long)(0x56fd23c8f9715a4cL)), unchecked((long)
			(0xcd9a497658a5698L)), unchecked((long)(0x5a110c6058b920a0L)), unchecked((long)(
			0x4208fe9e8f7f2d6L)), unchecked((long)(0x7a249a57ec0c9ba2L)), unchecked((long)(0x1d1260a51107fe97L
			)), unchecked((long)(0x722ff175f572c348L)), unchecked((long)(0x5e11e86d5873d484L
			)), unchecked((long)(0xed9b915c66ed37eL)), unchecked((long)(0xb0183db56ffc6a79L)
			), unchecked((long)(0x506e6744cd974924L)), unchecked((long)(0x881b82a13b51b9e2L)
			), unchecked((long)(0x9a9632e65904ad3cL)), unchecked((long)(0x742e1e651c60ba83L)
			), unchecked((long)(0x4feabfbbdb619cbL)), unchecked((long)(0x48cbff086ddf285aL))
			, unchecked((long)(0x99e7afeabe000731L)), unchecked((long)(0x93c42566aef98ffbL))
			, unchecked((long)(0xa865a54edcc0f019L)), unchecked((long)(0xd151d86adb73615L)), 
			unchecked((long)(0xdab9fe6525d89021L)), unchecked((long)(0x1b85d488d0f20cc5L)), 
			unchecked((long)(0xf678647e3519ac6eL)) }, new long[] { unchecked((long)(0xdd2c5bc84bc8d8fcL
			)), unchecked((long)(0xae623fd67468aa70L)), unchecked((long)(0xff6712ffcfd75ea1L
			)), unchecked((long)(0x930f80f4e8eb7462L)), unchecked((long)(0x45f20042f24f1768L
			)), unchecked((long)(0xbb215798d45df7afL)), unchecked((long)(0xefac4b70633b8f81L
			)), unchecked((long)(0x56436c9fe1a1aa8dL)), unchecked((long)(0xaa969b5c691ccb7aL
			)), unchecked((long)(0x43539603d6c55602L)), unchecked((long)(0x1bede3a3aef53302L
			)), unchecked((long)(0xdec468145b7605f6L)), unchecked((long)(0x808bd68e6ac10365L
			)), unchecked((long)(0xc91800e98fb99929L)), unchecked((long)(0x22fe545401165f1cL
			)), unchecked((long)(0x7eed120d54cf2dd9L)), unchecked((long)(0x28aed140be0bb7ddL
			)), unchecked((long)(0x10cff333e0ed804aL)), unchecked((long)(0x91b859e59ecb6350L
			)), unchecked((long)(0xb415938d7da94e3cL)), unchecked((long)(0x21f08570f420e565L
			)), unchecked((long)(0xded2d633cad004f6L)), unchecked((long)(0x65942c7b3c7e11aeL
			)), unchecked((long)(0xa87832d392efee56L)), unchecked((long)(0xaef3af4a563dfe43L
			)), unchecked((long)(0x480412bab7f5be2aL)), unchecked((long)(0xaf2042f5cc5c2858L
			)), unchecked((long)(0xef2f054308f6a2bcL)), unchecked((long)(0x9bc5a38ef729abd4L
			)), unchecked((long)(0x2d255069f0b7dab3L)), unchecked((long)(0x5648f680f11a2741L
			)), unchecked((long)(0xc5cc1d89724fa456L)), unchecked((long)(0x4dc4de189b671a1cL
			)), unchecked((long)(0x66f70b33fe09017L)), unchecked((long)(0x9da4243de836994fL)
			), unchecked((long)(0xbce5d2248682c115L)), unchecked((long)(0x11379625747d5af3L)
			), unchecked((long)(0xf4f076e65f2ce6f0L)), unchecked((long)(0x52593803dff1e840L)
			), unchecked((long)(0x19afe59ae451497fL)), unchecked((long)(0xf793c46702e086a0L)
			), unchecked((long)(0x763c4a1371b368fdL)), unchecked((long)(0x2df16f761598aa4fL)
			), unchecked((long)(0x21a007933a522a20L)), unchecked((long)(0xb3819a42abe61c87L)
			), unchecked((long)(0xb46ee9c5e64a6e7cL)), unchecked((long)(0xc07a3f80c31fb4b4L)
			), unchecked((long)(0x51039ab7712457c3L)), unchecked((long)(0x9ae182c8bc9474e8L)
			), unchecked((long)(0xb05ca3f564268d99L)), unchecked((long)(0xcfc447f1e53c8e1bL)
			), unchecked((long)(0x4850e73e03eb6064L)), unchecked((long)(0x2c604a7a177326b3L)
			), unchecked((long)(0xbf692b38d079f23L)), unchecked((long)(0xde336a2a4bc1c44bL))
			, unchecked((long)(0xd7288e012aeb8d31L)), unchecked((long)(0x6703df9d2924e97eL))
			, unchecked((long)(0x8ec97d2917456ed0L)), unchecked((long)(0x9c684cb6c4d24417L))
			, unchecked((long)(0xfc6a82d64b8655fbL)), unchecked((long)(0xf9b5b7c4acc67c96L))
			, unchecked((long)(0x69b97db1a4c03dfeL)), unchecked((long)(0xe755178d58fc4e76L))
			, unchecked((long)(0xa4fc4bd4fc5558caL)) } };

		public static readonly long[][] bishop = new long[][] { new long[] { unchecked((long
			)(0x2fe4b17170e59750L)), unchecked((long)(0xe8d9ecbe2cf3d73fL)), unchecked((long
			)(0xb57d2e985e1419c7L)), unchecked((long)(0x572b974f03ce0bbL)), unchecked((long)
			(0xa8d7e4dab780a08dL)), unchecked((long)(0x4715ed43e8a45c0aL)), unchecked((long)
			(0xc330de426430f69dL)), unchecked((long)(0x23b70edb1955c4bfL)), unchecked((long)
			(0x49353fea39ba63b1L)), unchecked((long)(0xf85b2b4fbcde44b7L)), unchecked((long)
			(0xbe7444e39328a0acL)), unchecked((long)(0x3e2b8bcbf016d66dL)), unchecked((long)
			(0x964e915cd5e2b207L)), unchecked((long)(0x1725cabfcb045b00L)), unchecked((long)
			(0x7fbf21ec8a1f45ecL)), unchecked((long)(0x11317ba87905e790L)), unchecked((long)
			(0xe94c39a54a98307fL)), unchecked((long)(0xaa70b5b4f89695a2L)), unchecked((long)
			(0x3bdbb92c43b17f26L)), unchecked((long)(0xcccb7005c6b9c28dL)), unchecked((long)
			(0x18a6a990c8b35ebdL)), unchecked((long)(0xfc7c95d827357afaL)), unchecked((long)
			(0x1fca8a92fd719f85L)), unchecked((long)(0x1dd01aafcd53486aL)), unchecked((long)
			(0xdbc0d2b6ab90a559L)), unchecked((long)(0x94628d38d0c20584L)), unchecked((long)
			(0x64972d68dee33360L)), unchecked((long)(0xb9c11d5b1e43a07eL)), unchecked((long)
			(0x2de0966daf2f8b1cL)), unchecked((long)(0x2e18bc1ad9704a68L)), unchecked((long)
			(0xd4dba84729af48adL)), unchecked((long)(0xb7a0b174cff6f36eL)), unchecked((long)
			(0xcffe1939438e9b24L)), unchecked((long)(0x79999cdff70902cbL)), unchecked((long)
			(0x8547eddfb81ccb94L)), unchecked((long)(0x7b77497b32503b12L)), unchecked((long)
			(0x97fcaacbf030bc24L)), unchecked((long)(0x6ced1983376fa72bL)), unchecked((long)
			(0x7e75d99d94a70f4dL)), unchecked((long)(0xd2733c4335c6a72fL)), unchecked((long)
			(0x9ff38fed72e9052fL)), unchecked((long)(0x9f65789a6509a440L)), unchecked((long)
			(0x981dcd296a8736dL)), unchecked((long)(0x5873888850659ae7L)), unchecked((long)(
			0xc678b6d860284a1cL)), unchecked((long)(0x63e22c147b9c3403L)), unchecked((long)(
			0x92fae24291f2b3f1L)), unchecked((long)(0x829626e3892d95d7L)), unchecked((long)(
			0x7a76956c3eafb413L)), unchecked((long)(0x7f5126dbba5e0ca7L)), unchecked((long)(
			0x12153635b2c0cf57L)), unchecked((long)(0x7b3f0195fc6f290fL)), unchecked((long)(
			0x5544f7d774b14aefL)), unchecked((long)(0x56c074a581ea17feL)), unchecked((long)(
			0xe7f28ecd2d49eecdL)), unchecked((long)(0xe479ee5b9930578cL)), unchecked((long)(
			0x7f9d1a2e1ebe1327L)), unchecked((long)(0x5d0a12f27ad310d1L)), unchecked((long)(
			0x3bc36e078f7515d7L)), unchecked((long)(0x4da8979a0041e8a9L)), unchecked((long)(
			0x950113646d1d6e03L)), unchecked((long)(0x7b4a38e32537df62L)), unchecked((long)(
			0x8a1b083821f40cb4L)), unchecked((long)(0x3d5774a11d31ab39L)) }, new long[] { unchecked(
			(long)(0x501f65edb3034d07L)), unchecked((long)(0x907f30421d78c5deL)), unchecked(
			(long)(0x1a804aadb9cfa741L)), unchecked((long)(0xce2a38c344a6eedL)), unchecked((
			long)(0xd363eff5f0977996L)), unchecked((long)(0x2cd16e2abd791e33L)), unchecked((
			long)(0x58627e1a149bba21L)), unchecked((long)(0x7f9b6af1ebf78bafL)), unchecked((
			long)(0x364f6ffa464ee52eL)), unchecked((long)(0x6c3b8e3e336139d3L)), unchecked((
			long)(0xf943aee7febf21b8L)), unchecked((long)(0x88e049589c432e0L)), unchecked((long
			)(0xd49503536abca345L)), unchecked((long)(0x3a6c27934e31188aL)), unchecked((long
			)(0x957baf61700cff4eL)), unchecked((long)(0x37624ae5a48fa6e9L)), unchecked((long
			)(0xb344c470397bba52L)), unchecked((long)(0xbac7a9a18531294bL)), unchecked((long
			)(0xecb53939887e8175L)), unchecked((long)(0x565601c0364e3228L)), unchecked((long
			)(0xef1955914b609f93L)), unchecked((long)(0x16f50edf91e513afL)), unchecked((long
			)(0x56963b0dca418fc0L)), unchecked((long)(0xd60f6dcedc314222L)), unchecked((long
			)(0x99170a5dc3115544L)), unchecked((long)(0x59b97885e2f2ea28L)), unchecked((long
			)(0xbc4097b116c524d2L)), unchecked((long)(0x7a13f18bbedc4ff5L)), unchecked((long
			)(0x71582401c38434dL)), unchecked((long)(0xb422061193d6f6a7L)), unchecked((long)
			(0xb4b81b3fa97511e2L)), unchecked((long)(0x65d34954daf3cebdL)), unchecked((long)
			(0xc7d9f16864a76e94L)), unchecked((long)(0x7bd94e1d8e17debcL)), unchecked((long)
			(0xd873db391292ed4fL)), unchecked((long)(0x30f5611484119414L)), unchecked((long)
			(0x565c31f7de89ea27L)), unchecked((long)(0xd0e4366228b03343L)), unchecked((long)
			(0x325928ee6e6f8794L)), unchecked((long)(0x6f423357e7c6a9f9L)), unchecked((long)
			(0x35dd37d5871448afL)), unchecked((long)(0xb03031a8b4516e84L)), unchecked((long)
			(0xb3f256d8aca0b0b9L)), unchecked((long)(0xfd22063edc29fcaL)), unchecked((long)(
			0xd9a11fbb3d9808e4L)), unchecked((long)(0x3a9bf55ba91f81caL)), unchecked((long)(
			0xc8c93882f9475f5fL)), unchecked((long)(0x947ae053ee56e63cL)), unchecked((long)(
			0xbbe83f4ecc2bdecbL)), unchecked((long)(0xcd454f8f19c5126aL)), unchecked((long)(
			0xc62c58f97dd949bfL)), unchecked((long)(0x693501d628297551L)), unchecked((long)(
			0xb9ab4ce57f2d34f3L)), unchecked((long)(0x9255abb50d532280L)), unchecked((long)(
			0xebfafa33d7254b59L)), unchecked((long)(0xe9f6082b05542e4eL)), unchecked((long)(
			0x98954d51fff6580L)), unchecked((long)(0x8107fccf064fcf56L)), unchecked((long)(0x852f54934da55cc9L
			)), unchecked((long)(0x9c7e552bc76492fL)), unchecked((long)(0xe9f6760e32cd8021L)
			), unchecked((long)(0xa3bc941d0a5061cbL)), unchecked((long)(0xba89142e007503b8L)
			), unchecked((long)(0xdc842b7e2819e230L)) } };

		public static readonly long[][] queen = new long[][] { new long[] { unchecked((long
			)(0x720bf5f26f4d2eaaL)), unchecked((long)(0x1c2559e30f0946beL)), unchecked((long
			)(0xe328e230e3e2b3fbL)), unchecked((long)(0x87e79e5a57d1d13L)), unchecked((long)
			(0x8dd9bdfd96b9f63L)), unchecked((long)(0x64d0e29eea8838b3L)), unchecked((long)(
			0xddf957bc36d8b9caL)), unchecked((long)(0x6ffe73e81b637fb3L)), unchecked((long)(
			0x93b633abfa3469f8L)), unchecked((long)(0xe846963877671a17L)), unchecked((long)(
			0x59ac2c7873f910a3L)), unchecked((long)(0x660d3257380841eeL)), unchecked((long)(
			0xd813f2fab7f5c5caL)), unchecked((long)(0x4112cf68649a260eL)), unchecked((long)(
			0x443f64ec5a371195L)), unchecked((long)(0xb0774d261cc609dbL)), unchecked((long)(
			0xb5635c95ff7296e2L)), unchecked((long)(0xed2df21216235097L)), unchecked((long)(
			0x4a29c6465a314cd1L)), unchecked((long)(0xd83cc2687a19255fL)), unchecked((long)(
			0x506c11b9d90e8b1dL)), unchecked((long)(0x57277707199b8175L)), unchecked((long)(
			0xcaf21ecd4377b28cL)), unchecked((long)(0xc0c0f5a60ef4cdcfL)), unchecked((long)(
			0x7c45d833aff07862L)), unchecked((long)(0xa5b1cfdba0ab4067L)), unchecked((long)(
			0x6ad047c430a12104L)), unchecked((long)(0x6c47bec883a7de39L)), unchecked((long)(
			0x944f6de09134dfb6L)), unchecked((long)(0x9aeba33ac6ecc6b0L)), unchecked((long)(
			0x52e762596bf68235L)), unchecked((long)(0x22af003ab672e811L)), unchecked((long)(
			0x50065e535a213cf6L)), unchecked((long)(0xde0c89a556b9ae70L)), unchecked((long)(
			0xd1e0ccd25bb9c169L)), unchecked((long)(0x6b17b224bad6bf27L)), unchecked((long)(
			0x6b02e63195ad0cf8L)), unchecked((long)(0x455a4b4cfe30e3f5L)), unchecked((long)(
			0x9338e69c052b8e7bL)), unchecked((long)(0x5092ef950a16da0bL)), unchecked((long)(
			0x67fef95d92607890L)), unchecked((long)(0x31865ced6120f37dL)), unchecked((long)(
			0x3a6853c7e70757a7L)), unchecked((long)(0x32ab0edb696703d3L)), unchecked((long)(
			0xee97f453f06791edL)), unchecked((long)(0x6dc93d9526a50e68L)), unchecked((long)(
			0x78edefd694af1eedL)), unchecked((long)(0x9c1169fa2777b874L)), unchecked((long)(
			0x6bfa9aae5ec05779L)), unchecked((long)(0x371f77e76bb8417eL)), unchecked((long)(
			0x3550c2321fd6109cL)), unchecked((long)(0xfb4a3d794a9a80d2L)), unchecked((long)(
			0xf43c732873f24c13L)), unchecked((long)(0xaa9119ff184cccf4L)), unchecked((long)(
			0xb69e38a8965c6b65L)), unchecked((long)(0x1f2b1d1f15f6dc9cL)), unchecked((long)(
			0xb5b4071dbfc73a66L)), unchecked((long)(0x8f9887e6078735a1L)), unchecked((long)(
			0x8de8a1c7797da9bL)), unchecked((long)(0xfcb6be43a9f2fe9bL)), unchecked((long)(0x49a7f41061a9e60L
			)), unchecked((long)(0x9f91508bffcfc14aL)), unchecked((long)(0xe3273522064480caL
			)), unchecked((long)(0xcd04f3ff001a4778L)) }, new long[] { unchecked((long)(0x1bda0492e7e4586eL
			)), unchecked((long)(0xd23c8e176d113600L)), unchecked((long)(0x252f59cf0d9f04bbL
			)), unchecked((long)(0xb3598080ce64a656L)), unchecked((long)(0x993e1de72d36d310L
			)), unchecked((long)(0xa2853b80f17f58eeL)), unchecked((long)(0x1877b51e57a764d5L
			)), unchecked((long)(0x1f837cc7350524L)), unchecked((long)(0x241260ed4ad1e87dL))
			, unchecked((long)(0x64c8e531bff53b55L)), unchecked((long)(0xca672b91e9e4fa16L))
			, unchecked((long)(0x3871700761b3f743L)), unchecked((long)(0xf95cffa23af5f6f4L))
			, unchecked((long)(0x8d14dedb30be846eL)), unchecked((long)(0x3b097adaf088f94eL))
			, unchecked((long)(0x21e0bd5026c619bfL)), unchecked((long)(0xb8d91274b9e9d4fbL))
			, unchecked((long)(0x1db956e450275779L)), unchecked((long)(0x4fc8e9560f91b123L))
			, unchecked((long)(0x63573ff03e224774L)), unchecked((long)(0x647dfedcd894a29L)), 
			unchecked((long)(0x7884d9bc6cb569d8L)), unchecked((long)(0x7fba195410e5ca30L)), 
			unchecked((long)(0x106c09b972d2e822L)), unchecked((long)(0x98f076a4f7a2322eL)), 
			unchecked((long)(0x70cb6af7c2d5bcf0L)), unchecked((long)(0xb64be8d8b25396c1L)), 
			unchecked((long)(0xa9aa4d20db084e9bL)), unchecked((long)(0x2e6d02c36017f67fL)), 
			unchecked((long)(0xefed53d75fd64e6bL)), unchecked((long)(0xd9f1f30ccd97fb09L)), 
			unchecked((long)(0xa2ebee47e2fbfce1L)), unchecked((long)(0xfc87614baf287e07L)), 
			unchecked((long)(0x240ab57a8b888b20L)), unchecked((long)(0xbf8d5108e27e0d48L)), 
			unchecked((long)(0x61bdd1307c66e300L)), unchecked((long)(0xb925a6cd0421aff3L)), 
			unchecked((long)(0x3e003e616a6591e9L)), unchecked((long)(0x94c3251f06f90cf3L)), 
			unchecked((long)(0xbf84470805e69b5fL)), unchecked((long)(0x758f450c88572e0bL)), 
			unchecked((long)(0x1b6baca2ae4e125bL)), unchecked((long)(0x61cf4f94c97df93dL)), 
			unchecked((long)(0x2738259634305c14L)), unchecked((long)(0xd39bb9c3a48db6cfL)), 
			unchecked((long)(0x8215e577001332c8L)), unchecked((long)(0xa1082c0466df6c0aL)), 
			unchecked((long)(0xef02cdd06ffdb432L)), unchecked((long)(0x7976033a39f7d952L)), 
			unchecked((long)(0x106f72fe81e2c590L)), unchecked((long)(0x8c90fd9b083f4558L)), 
			unchecked((long)(0xfd080d236da814baL)), unchecked((long)(0x7b64978555326f9fL)), 
			unchecked((long)(0x60e8ed72c0dff5d1L)), unchecked((long)(0xb063e962e045f54dL)), 
			unchecked((long)(0x959f587d507a8359L)), unchecked((long)(0x1a4e4822eb4d7a59L)), 
			unchecked((long)(0x5d94337fbfaf7f5bL)), unchecked((long)(0xd30c088ba61ea5efL)), 
			unchecked((long)(0x9d765e419fb69f6dL)), unchecked((long)(0x9e21f4f903b33fd9L)), 
			unchecked((long)(0xb4d8f77bc3e56167L)), unchecked((long)(0x733ea705fae4fa77L)), 
			unchecked((long)(0xa4ec0132764ca04bL)) } };

		public static readonly long[][] king = new long[][] { new long[] { unchecked((long
			)(0x2102ae466ebb1148L)), unchecked((long)(0xe87fbb46217a360eL)), unchecked((long
			)(0x310cb380db6f7503L)), unchecked((long)(0xb5fdfc5d3132c498L)), unchecked((long
			)(0xdaf8e9829fe96b5fL)), unchecked((long)(0xcac09afbddd2cdb4L)), unchecked((long
			)(0xb862225b055b6960L)), unchecked((long)(0x55b6344cf97aafaeL)), unchecked((long
			)(0x46e3ecaaf453ce9L)), unchecked((long)(0x962aceefa82e1c84L)), unchecked((long)
			(0xf5b4b0b0d2deeeb4L)), unchecked((long)(0x1af3dbe25d8f45daL)), unchecked((long)
			(0xf9f4892ed96bd438L)), unchecked((long)(0xc4c118bfe78feaaeL)), unchecked((long)
			(0x7a69afdcc42261aL)), unchecked((long)(0xf8549e1a3aa5e00dL)), unchecked((long)(
			0x486289ddcc3d6780L)), unchecked((long)(0x222bbfae61725606L)), unchecked((long)(
			0x2bc60a63a6f3b3f2L)), unchecked((long)(0x177e00f9fc32f791L)), unchecked((long)(
			0x522e23f3925e319eL)), unchecked((long)(0x9c2ed44081ce5fbdL)), unchecked((long)(
			0x964781ce734b3c84L)), unchecked((long)(0xf05d129681949a4cL)), unchecked((long)(
			0xd586bd01c5c217f6L)), unchecked((long)(0x233003b5a6cfe6adL)), unchecked((long)(
			0x24c0e332b70019b0L)), unchecked((long)(0x9da058c67844f20cL)), unchecked((long)(
			0xe4d9429322cd065aL)), unchecked((long)(0x1fab64ea29a2ddf7L)), unchecked((long)(
			0x8af38731c02ba980L)), unchecked((long)(0x7dc7785b8efdfc80L)), unchecked((long)(
			0x93cbe0b699c2585dL)), unchecked((long)(0x1d95b0a5fcf90bc6L)), unchecked((long)(
			0x17efee45b0dee640L)), unchecked((long)(0x9e4c1269baa4bf37L)), unchecked((long)(
			0xd79476a84ee20d06L)), unchecked((long)(0xa56a5f0bfe39272L)), unchecked((long)(0x7eba726d8c94094bL
			)), unchecked((long)(0x5e5637885f29bc2bL)), unchecked((long)(0xc61bb3a141e50e8cL
			)), unchecked((long)(0x2785338347f2ba08L)), unchecked((long)(0x7ca9723fbb2e8988L
			)), unchecked((long)(0xce2f8642ca0712dcL)), unchecked((long)(0x59300222b4561e00L
			)), unchecked((long)(0xc2b5a03f71471a6fL)), unchecked((long)(0xd5f9e858292504d5L
			)), unchecked((long)(0x65fa4f227a2b6d79L)), unchecked((long)(0x71f1ce2490d20b07L
			)), unchecked((long)(0xe6c42178c4bbb92eL)), unchecked((long)(0xa9c32d5eae45305L)
			), unchecked((long)(0xc335248857fa9e7L)), unchecked((long)(0x142de49fff7a7c3dL))
			, unchecked((long)(0x64a53dc924fe7ac9L)), unchecked((long)(0x9f6a419d382595f4L))
			, unchecked((long)(0x150f361dab9dec26L)), unchecked((long)(0xd20d8c88c8ffe65fL))
			, unchecked((long)(0x917f1dd5f8886c61L)), unchecked((long)(0x56986e2ef3ed091bL))
			, unchecked((long)(0x5fa7867caf35e149L)), unchecked((long)(0x81a1549fd6573da5L))
			, unchecked((long)(0x96fbf83a12884624L)), unchecked((long)(0xe728e8c83c334074L))
			, unchecked((long)(0xf1bcc3d275afe51aL)) }, new long[] { unchecked((long)(0xd6b04d3b7651dd7eL
			)), unchecked((long)(0xe34a1d250e7a8d6bL)), unchecked((long)(0x53c065c6c8e63528L
			)), unchecked((long)(0x1bdea12e35f6a8c9L)), unchecked((long)(0x21874b8b4d2dbc4fL
			)), unchecked((long)(0x3a88a0fbbcb05c63L)), unchecked((long)(0x43ed7f5a0fae657dL
			)), unchecked((long)(0x230e343dfba08d33L)), unchecked((long)(0xd4c718bc4ae8ae5fL
			)), unchecked((long)(0x9eedeca8e272b933L)), unchecked((long)(0x10e8b35af3eeab37L
			)), unchecked((long)(0xe09b88e1914f7afL)), unchecked((long)(0x3fa9ddfb67e2f199L)
			), unchecked((long)(0xb10bb459132d0a26L)), unchecked((long)(0x2c046f22062dc67dL)
			), unchecked((long)(0x5e90277e7cb39e2dL)), unchecked((long)(0xb49b52e587a1ee60L)
			), unchecked((long)(0xac042e70f8b383f2L)), unchecked((long)(0x89c350c893ae7dc1L)
			), unchecked((long)(0xb592bf39b0364963L)), unchecked((long)(0x190e714fada5156eL)
			), unchecked((long)(0xec8177f83f900978L)), unchecked((long)(0x91b534f885818a06L)
			), unchecked((long)(0x81536d601170fc20L)), unchecked((long)(0x57e3306d881edb4fL)
			), unchecked((long)(0xa804d18b7097475L)), unchecked((long)(0xe74733427b72f0c1L))
			, unchecked((long)(0x24b33c9d7ed25117L)), unchecked((long)(0xe805a1e290cf2456L))
			, unchecked((long)(0x3b544ebe544c19f9L)), unchecked((long)(0x3e666e6f69ae2c15L))
			, unchecked((long)(0xfb152fe3ff26da89L)), unchecked((long)(0x1a4ff12616eefc89L))
			, unchecked((long)(0x990a98fd5071d263L)), unchecked((long)(0x84547ddc3e203c94L))
			, unchecked((long)(0x7a3aec79624c7daL)), unchecked((long)(0x8a328a1cedfe552cL)), 
			unchecked((long)(0xd1e649de1e7f268bL)), unchecked((long)(0x2d8d5432157064c8L)), 
			unchecked((long)(0x4ae7d6a36eb5dbcbL)), unchecked((long)(0x4659d2b743848a2cL)), 
			unchecked((long)(0x19ebb029435dcb0fL)), unchecked((long)(0x4e9d2827355fc492L)), 
			unchecked((long)(0xccec0a73b49c9921L)), unchecked((long)(0x46c9feb55d120902L)), 
			unchecked((long)(0x8d2636b81555a786L)), unchecked((long)(0x30c05b1ba332f41cL)), 
			unchecked((long)(0xf6f7fd1431714200L)), unchecked((long)(0xabbdcdd7ed5c0860L)), 
			unchecked((long)(0x9853eab63b5e0b35L)), unchecked((long)(0x352787baa0d7c22fL)), 
			unchecked((long)(0xc7f6aa2de59aea61L)), unchecked((long)(0x3727073c2e134b1L)), unchecked(
			(long)(0x5a0f544dd2b1fb18L)), unchecked((long)(0x74f85198b05a2e7dL)), unchecked(
			(long)(0x963ef2c96b33be31L)), unchecked((long)(0xff577222c14f0a3aL)), unchecked(
			(long)(0x4e4b705b92903ba4L)), unchecked((long)(0x730499af921549ffL)), unchecked(
			(long)(0x13ae978d09fe5557L)), unchecked((long)(0xd9e92aa246bf719eL)), unchecked(
			(long)(0x7a4c10ec2158c4a6L)), unchecked((long)(0x49cad48cebf4a71eL)), unchecked(
			(long)(0xcf05daf5ac8d77b0L)) } };

		public const long whiteKingSideCastling = unchecked((long)(0x31d71dce64b2c310L));

		public const long whiteQueenSideCastling = unchecked((long)(0xf165b587df898190L));

		public const long blackKingSideCastling = unchecked((long)(0xa57e6339dd2cf3a0L));

		public const long blackQueenSideCastling = unchecked((long)(0x1ef6e6dbb1961ec9L));

		public static readonly long[] passantColumn = new long[] { unchecked((long)(0x70cc73d90bc26e24L
			)), unchecked((long)(0xe21a6b35df0c3ad7L)), unchecked((long)(0x3a93d8b2806962L))
			, unchecked((long)(0x1c99ded33cb890a1L)), unchecked((long)(0xcf3145de0add4289L))
			, unchecked((long)(0xd0e4427a5514fb72L)), unchecked((long)(0x77c621cc9fb3a483L))
			, unchecked((long)(0x67a34dac4356550bL)) };

		public const long whiteMove = unchecked((long)(0xf8d626aaaf278509L));

		public static long GetKeyPieceIndex(int index, char pieceChar)
		{
			switch (pieceChar)
			{
				case 'P':
				{
					return pawn[0][index];
				}

				case 'p':
				{
					return pawn[1][index];
				}

				case 'R':
				{
					return rook[0][index];
				}

				case 'r':
				{
					return rook[1][index];
				}

				case 'N':
				{
					return knight[0][index];
				}

				case 'n':
				{
					return knight[1][index];
				}

				case 'B':
				{
					return bishop[0][index];
				}

				case 'b':
				{
					return bishop[1][index];
				}

				case 'Q':
				{
					return queen[0][index];
				}

				case 'q':
				{
					return queen[1][index];
				}

				case 'K':
				{
					return king[0][index];
				}

				case 'k':
				{
					return king[1][index];
				}
			}
			return 0;
		}

		public static long[] GetKey(Board board)
		{
			long[] key = new long[] { 0, 0 };
			long square = BitboardUtils.H1;
			byte index = 0;
			int color = 0;
			while (square != 0)
			{
				color = (square & board.whites) != 0 ? 0 : 1;
				key[color] ^= GetKeyPieceIndex(index, board.GetPieceAt(square));
				square <<= 1;
				index++;
			}
			if (board.GetWhiteKingsideCastling())
			{
				key[0] ^= whiteKingSideCastling;
			}
			if (board.GetWhiteQueensideCastling())
			{
				key[0] ^= whiteQueenSideCastling;
			}
			if (board.GetBlackKingsideCastling())
			{
				key[1] ^= blackKingSideCastling;
			}
			if (board.GetBlackQueensideCastling())
			{
				key[1] ^= blackQueenSideCastling;
			}
			// passant flags only when pawn can capture
			long passant = board.GetPassantSquare();
			if ((passant != 0) && (((!board.GetTurn() && (((passant << 9) | (passant << 7)) &
				 board.blacks & board.pawns) != 0)) || ((board.GetTurn() && ((((long)(((ulong)passant
				) >> 9)) | ((long)(((ulong)passant) >> 7))) & board.whites & board.pawns) != 0))
				))
			{
				color = board.GetTurn() ? 0 : 1;
				// TODO test
				key[1 - color] ^= passantColumn[BitboardUtils.GetColumn(passant)];
			}
			if (board.GetTurn())
			{
				key[0] ^= whiteMove;
			}
			return key;
		}
	}
}
